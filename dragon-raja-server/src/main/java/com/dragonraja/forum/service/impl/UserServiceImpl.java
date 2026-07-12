package com.dragonraja.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragonraja.forum.common.PageResult;
import com.dragonraja.forum.dto.AuditUserDTO;
import com.dragonraja.forum.dto.LoginDTO;
import com.dragonraja.forum.dto.RegisterDTO;
import com.dragonraja.forum.dto.UpdateProfileDTO;
import com.dragonraja.forum.entity.Comment;
import com.dragonraja.forum.entity.Friend;
import com.dragonraja.forum.entity.Message;
import com.dragonraja.forum.entity.Post;
import com.dragonraja.forum.entity.User;
import com.dragonraja.forum.enums.OnlineStatus;
import com.dragonraja.forum.enums.UserStatus;
import com.dragonraja.forum.exception.BusinessException;
import com.dragonraja.forum.mapper.CommentMapper;
import com.dragonraja.forum.mapper.FriendMapper;
import com.dragonraja.forum.mapper.MessageMapper;
import com.dragonraja.forum.mapper.PostMapper;
import com.dragonraja.forum.mapper.UserMapper;
import com.dragonraja.forum.security.JwtUtils;
import com.dragonraja.forum.security.UserContext;
import com.dragonraja.forum.service.UserService;
import com.dragonraja.forum.vo.LoginVO;
import com.dragonraja.forum.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 用户服务实现类
 *
 * @author Kou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final MessageMapper messageMapper;
    private final FriendMapper friendMapper;
    private final JwtUtils jwtUtils;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /** BCrypt 密码编码器 */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /** 日期格式化 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String register(RegisterDTO dto) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 构建用户实体
        User user = new User();
        user.setUsername(dto.getUsername());
        // BCrypt 加密密码
        user.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setBloodlineGrade(StringUtils.hasText(dto.getBloodlineGrade()) ? dto.getBloodlineGrade() : "D");
        user.setFaction(dto.getFaction());
        user.setYanling(dto.getYanling());
        user.setBloodType(dto.getBloodType());
        user.setAvatar(dto.getAvatar());
        // 注册用户默认状态为待审核
        user.setStatus(UserStatus.PENDING.getCode());
        user.setOnlineStatus(OnlineStatus.OFFLINE.getCode());
        // 默认角色为普通用户
        user.setRole("USER");

        userMapper.insert(user);
        log.info("用户注册成功: {}", dto.getUsername());

        // 通知所有管理员和审核员有新用户注册
        try {
            LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
            adminWrapper.in(User::getRole, "ADMIN", "AUDITOR");
            List<User> admins = userMapper.selectList(adminWrapper);
            LocalDateTime now = LocalDateTime.now();
            for (User admin : admins) {
                Message msg = new Message();
                msg.setReceiverId(admin.getId());
                msg.setTitle("新用户注册待审核");
                String nick = StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername();
                msg.setContent("用户「" + nick + "」刚刚注册了账号，请前往注册审核处理。");
                msg.setSender("诺玛");
                msg.setSenderId(1L);
                msg.setType("SYSTEM");
                msg.setIsRead(0);
                msg.setCreateTime(now);
                messageMapper.insert(msg);
            }
            log.info("已通知 {} 名管理员/审核员", admins.size());
        } catch (Exception e) {
            log.warn("发送注册通知失败", e);
        }

        return "注册成功，等待管理员审核";
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 根据用户名查询用户
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验密码
        if (!PASSWORD_ENCODER.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验用户状态
        if (user.getStatus() == UserStatus.PENDING.getCode()) {
            throw new BusinessException("账号待审核，请联系管理员");
        }
        if (user.getStatus() == UserStatus.BANNED.getCode()) {
            throw new BusinessException("账号已被封禁，请联系管理员");
        }

        // 生成 JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 更新在线状态为在线
        user.setOnlineStatus(OnlineStatus.ONLINE.getCode());
        userMapper.updateById(user);

        // 组装返回 VO
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());

        log.info("用户登录成功: {}", dto.getUsername());
        return vo;
    }

    @Override
    public UserVO getCurrentUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 仅更新传入的字段
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            // 删除旧头像文件
            String oldAvatar = user.getAvatar();
            if (oldAvatar != null && oldAvatar.startsWith("/uploads/avatars/")) {
                try {
                    Path oldFile = Paths.get(uploadDir, "avatars", oldAvatar.replace("/uploads/avatars/", ""));
                    Files.deleteIfExists(oldFile);
                    log.info("已删除旧头像文件: {}", oldFile);
                } catch (IOException e) {
                    log.warn("删除旧头像文件失败: {} - {}", oldAvatar, e.getMessage());
                }
            }
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getSignature() != null) {
            user.setSignature(dto.getSignature());
        }
        // 血统等级不可通过个人资料修改（仅诺玛/3E测试可设定）
        if (StringUtils.hasText(dto.getFaction())) {
            user.setFaction(dto.getFaction());
        }
        if (dto.getYanling() != null) {
            user.setYanling(dto.getYanling());
        }

        userMapper.updateById(user);
        log.info("用户更新资料成功: userId={}", userId);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验旧密码
        if (!PASSWORD_ENCODER.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        user.setPassword(PASSWORD_ENCODER.encode(newPassword));
        userMapper.updateById(user);
        log.info("用户修改密码成功: userId={}", userId);
    }

    /** 永久在线用户 nickname 列表 */
    private static final List<String> ALWAYS_ONLINE_NICKNAMES = List.of("norma", "mrLiJIaTu");

    @Override
    public PageResult<UserVO> getOnlineUsers(Long current, Long size) {
        // 查询在线状态为1的用户（排除管理员）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOnlineStatus, OnlineStatus.ONLINE.getCode())
                .ne(User::getRole, "ADMIN")
                .orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(current, size);
        Page<User> resultPage = userMapper.selectPage(page, wrapper);

        // 永久在线用户（补入结果集，不限角色，按用户名匹配）
        List<User> alwaysOnline = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .in(User::getUsername, ALWAYS_ONLINE_NICKNAMES)
        );
        List<User> records = new java.util.ArrayList<>(resultPage.getRecords());
        for (User u : alwaysOnline) {
            boolean exists = records.stream().anyMatch(r -> r.getId().equals(u.getId()));
            if (!exists) {
                records.add(0, u);
            }
        }
        resultPage.setRecords(records);
        resultPage.setTotal(resultPage.getTotal() + (records.size() - resultPage.getRecords().size()));

        // 转换为 VO
        Page<UserVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(resultPage.getRecords().stream().map(this::convertToVO).toList());

        return PageResult.of(voPage);
    }

    @Override
    public PageResult<UserVO> getUserPage(Long current, Long size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(current, size);
        Page<User> resultPage = userMapper.selectPage(page, wrapper);

        // 转换为 VO
        Page<UserVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(resultPage.getRecords().stream().map(this::convertToVO).toList());

        return PageResult.of(voPage);
    }

    @Override
    public PageResult<UserVO> getPendingUserPage(Long current, Long size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 0)
               .orderByDesc(User::getCreateTime);
        Page<User> page = new Page<>(current, size);
        Page<User> resultPage = userMapper.selectPage(page, wrapper);
        Page<UserVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(resultPage.getRecords().stream().map(this::convertToVO).toList());
        return PageResult.of(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditUser(Long userId, AuditUserDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验用户当前状态为待审核
        if (user.getStatus() != UserStatus.PENDING.getCode()) {
            throw new BusinessException("该用户当前状态不支持审核操作");
        }

        // 校验审核结果：1-通过 2-拒绝
        if (dto.getStatus() != UserStatus.NORMAL.getCode() && dto.getStatus() != UserStatus.BANNED.getCode()) {
            throw new BusinessException("审核结果无效，仅支持 1-通过 或 2-拒绝");
        }

        user.setStatus(dto.getStatus());
        userMapper.updateById(user);

        // 审核通过时自动发送诺玛欢迎邮件
        if (dto.getStatus() == UserStatus.NORMAL.getCode()) {
            Message welcome = new Message();
            welcome.setReceiverId(userId);
            welcome.setTitle("欢迎加入卡塞尔学院社区");
            welcome.setContent(
                "学员 " + (user.getNickname() != null ? user.getNickname() : user.getUsername()) + "：\n\n" +
                "你的注册申请已通过审核，自即日起正式编入卡塞尔学院。\n\n" +
                "我是诺玛，卡塞尔学院中央控制系统，负责学院信息网络的运维与全体学员的信息支持。\n\n" +
                "你可通过以下渠道获取所需服务：\n" +
                "  学院新闻 — 龙族相关事件通报与学院行政公告\n" +
                "  混血种乐园 — 各派系学员交流平台，用于情报共享与委托发布\n" +
                "  邮件系统 — 学院通知分发及学员间正式通讯\n" +
                "  联系人 — 建立并维护你的学员联络网络\n\n" +
                "你的血统评级已录入系统。请严格遵循秘党守则，履行混血种之义务——\n" +
                "\"我们守望，我们战斗，直至永夜终结。\"\n\n" +
                "如需协助，我随时待命。\n\n" +
                "—— 诺玛\n" +
                "卡塞尔学院中央控制系统"
            );
            welcome.setSender("诺玛");
            welcome.setSenderId(1L);
            welcome.setType("SYSTEM");
            welcome.setIsRead(0);
            welcome.setCreateTime(LocalDateTime.now());
            messageMapper.insert(welcome);

            // 自动添加默认好友：norma(1) 和 mrLiJiaTu(3)，双方双向
            long[] defaultFriendIds = {1L, 3L, 16L};
            for (long fid : defaultFriendIds) {
                if (fid == userId) continue;
                // 正向：新用户 → 默认好友
                Friend fwd = new Friend();
                fwd.setUserId(userId); fwd.setFriendId(fid); fwd.setStatus(1);
                fwd.setCreateTime(LocalDateTime.now()); fwd.setUpdateTime(LocalDateTime.now());
                friendMapper.insert(fwd);
                // 反向：默认好友 → 新用户
                LambdaQueryWrapper<Friend> revCheck = new LambdaQueryWrapper<>();
                revCheck.eq(Friend::getUserId, fid).eq(Friend::getFriendId, userId);
                if (friendMapper.selectCount(revCheck) == 0) {
                    Friend rev = new Friend();
                    rev.setUserId(fid); rev.setFriendId(userId); rev.setStatus(1);
                    rev.setCreateTime(LocalDateTime.now()); rev.setUpdateTime(LocalDateTime.now());
                    friendMapper.insert(rev);
                }
            }
            log.info("审核通过并发送欢迎邮件: userId={}", userId);
        }

        log.info("审核用户完成: userId={}, status={}", userId, dto.getStatus());
    }

    @Override
    public void toggleBan(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 管理员不可被封禁
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException(400, "不可封禁管理员账号");
        }

        // 切换封禁状态：正常→封禁，封禁→正常
        if (user.getStatus() == UserStatus.BANNED.getCode()) {
            user.setStatus(UserStatus.NORMAL.getCode());
        } else {
            user.setStatus(UserStatus.BANNED.getCode());
        }

        userMapper.updateById(user);
        log.info("用户封禁状态切换: userId={}, status={}", userId, user.getStatus());
    }

    @Override
    public void logout(Long userId) {
        setOnlineStatus(userId, OnlineStatus.OFFLINE.getCode());
        log.info("用户登出: userId={}", userId);
    }

    @Override
    public void heartbeat(Long userId) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getOnlineStatus, OnlineStatus.ONLINE.getCode())
                .set(User::getLastActiveTime, LocalDateTime.now());
        userMapper.update(null, wrapper);
    }

    @Override
    @Scheduled(fixedRate = 60000)  // 每 1 分钟清理一次
    public void cleanupOfflineUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(3);
        // 排除永久在线用户
        List<Long> alwaysOnlineIds = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .select(User::getId)
                .in(User::getUsername, ALWAYS_ONLINE_NICKNAMES)
        ).stream().map(User::getId).toList();

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getOnlineStatus, OnlineStatus.ONLINE.getCode())
                .isNotNull(User::getLastActiveTime)
                .lt(User::getLastActiveTime, threshold)
                .set(User::getOnlineStatus, OnlineStatus.OFFLINE.getCode());
        if (!alwaysOnlineIds.isEmpty()) {
            wrapper.notIn(User::getId, alwaysOnlineIds);
        }
        int count = userMapper.update(null, wrapper);
        if (count > 0) {
            log.info("清理离线用户: {} 人（已排除永久在线用户）", count);
        }
    }

    @Override
    public void setOnlineStatus(Long userId, int onlineStatus) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getOnlineStatus, onlineStatus);
        userMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 不可删除管理员账号
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不可删除管理员账号");
        }

        // 不允许删除自己
        if (UserContext.getCurrentUserId() != null && UserContext.getCurrentUserId().equals(userId)) {
            throw new BusinessException("不可删除自己");
        }

        // 1. 删除该用户的所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getUserId, userId);
        commentMapper.delete(commentWrapper);

        // 2. 删除该用户的所有帖子
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(Post::getUserId, userId);
        postMapper.delete(postWrapper);

        // 3. 删除用户
        userMapper.deleteById(userId);

        log.warn("管理员删除用户: userId={}, username={}, operator={}",
                userId, user.getUsername(), UserContext.getCurrentUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOwnAccount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 管理员不能自行注销
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("管理员账号不可注销");
        }

        // 1. 删除该用户的所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getUserId, userId);
        commentMapper.delete(commentWrapper);

        // 2. 删除该用户的所有帖子
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(Post::getUserId, userId);
        postMapper.delete(postWrapper);

        // 3. 删除用户
        userMapper.deleteById(userId);

        log.warn("用户注销账号: userId={}, username={}", userId, user.getUsername());
    }

    @Override
    public void adminUpdateUser(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 仅更新传入的非空字段
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getSignature() != null) {
            user.setSignature(dto.getSignature());
        }
        if (StringUtils.hasText(dto.getBloodlineGrade())) {
            user.setBloodlineGrade(dto.getBloodlineGrade());
        }
        if (StringUtils.hasText(dto.getFaction())) {
            user.setFaction(dto.getFaction());
        }
        if (dto.getYanling() != null) {
            user.setYanling(dto.getYanling());
        }

        userMapper.updateById(user);
        log.info("管理员更新用户信息: userId={}, operator={}", userId, UserContext.getCurrentUsername());
    }

    /**
     * 将 User 实体转换为 UserVO
     *
     * @param user 用户实体
     * @return UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBloodlineGrade(user.getBloodlineGrade());
        vo.setFaction(user.getFaction());
        vo.setStatus(user.getStatus());
        vo.setOnlineStatus(user.getOnlineStatus());
        vo.setRole(user.getRole());
        vo.setSignature(user.getSignature());
        vo.setYanling(user.getYanling());
        vo.setBloodType(user.getBloodType());
        vo.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().format(DATE_FORMATTER) : null);
        return vo;
    }

    @Override
    public UserVO searchByKeyword(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1)
                .eq(User::getUsername, keyword);
        wrapper.last("LIMIT 1");
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(404, "未找到该学员");
        }
        return convertToVO(user);
    }

    private Long tryParseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }
}
