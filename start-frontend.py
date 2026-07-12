#!/usr/bin/env python
"""NightWatchMan Frontend Launcher"""
import subprocess
import os
import sys

FRONTEND_DIR = r"D:\DaiMa\NightWatchMan\dragon-raja-frontend"
NODE = r"D:\DaiMa\node.exe"
VITE = os.path.join(FRONTEND_DIR, "node_modules", "vite", "bin", "vite.js")

def main():
    os.chdir(FRONTEND_DIR)
    print("=" * 40)
    print("  NightWatchMan Frontend")
    print("  http://localhost:5175")
    print("=" * 40)
    print("Close this window to stop the server.\n")

    try:
        proc = subprocess.Popen(
            [NODE, VITE, "--port", "5175", "--host", "0.0.0.0"],
            cwd=FRONTEND_DIR,
            stdout=sys.stdout,
            stderr=sys.stderr,
        )
        proc.wait()
    except KeyboardInterrupt:
        print("\nServer stopped.")
    except Exception as e:
        print(f"\nError: {e}")
        input("Press Enter to exit...")

if __name__ == "__main__":
    main()
