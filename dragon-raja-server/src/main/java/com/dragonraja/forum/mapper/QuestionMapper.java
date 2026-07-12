package com.dragonraja.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dragonraja.forum.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    @Select("SELECT * FROM question ORDER BY RAND() LIMIT #{count}")
    List<Question> selectRandom(int count);

    @Select("SELECT * FROM question WHERE question = #{question} LIMIT 1")
    Question selectByQuestion(String question);

    @Select("<script>" +
            "SELECT * FROM question WHERE id != #{excludeId1}" +
            "<if test='excludeId2 != null'> AND id != #{excludeId2}</if>" +
            " ORDER BY RAND() LIMIT #{count}" +
            "</script>")
    List<Question> selectRandomExclude(int count, long excludeId1, Long excludeId2);
}
