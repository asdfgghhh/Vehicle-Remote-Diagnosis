# Windows路径映射说明

## 常见场景说明

### 1. WSL2 + Docker Desktop

如果使用 WSL2 运行 Docker Desktop：

```
Linux路径               Windows路径
─────────────────────────────────────
/workspace/            \\wsl$\<DistroName>\workspace\
/workspace/            C:\Users\<Username>\...  (取决于项目在Windows的位置)
```

### 2. VS Code Remote Containers

如果使用 VS Code Remote Containers：

```
Linux路径               Windows路径
─────────────────────────────────────
/workspace/            <你的Windows本地项目目录>\
```

### 3. Gitpod / GitHub Codespaces

如果使用在线开发环境：

```
Linux路径               Windows路径
─────────────────────────────────────
/workspace/            通过浏览器/本地编辑器访问
```

## 项目文件快速访问

### Windows文件位置

根据当前 `/workspace` 中的文件，在你的Windows机器上应该有对应的：

```
[你的Windows项目目录]/
├── backend/
├── frontend/
├── scripts/
├── docker-compose.yml
├── docker-compose-doris.yml
└── ...
```

### 在Windows中使用

#### 方式1：使用终端在Windows目录中操作

```powershell
# 切换到你的项目目录
cd C:\Users\你的用户名\Documents\vehicle-diagnosis

# 查看文件
dir
```

#### 方式2：文件浏览器直接访问

在文件资源管理器中打开你的项目目录。

## 常见命令对比

| 功能 | Linux/Mac | Windows (PowerShell) |
|------|-----------|---------------------|
| 列出目录 | `ls -la` | `dir` |
| 切换目录 | `cd /workspace` | `cd C:\你的路径` |
| 查看当前路径 | `pwd` | `Get-Location` |
| 创建目录 | `mkdir -p` | `New-Item -ItemType Directory` |
| 复制文件 | `cp` | `Copy-Item` |
| 删除文件 | `rm` | `Remove-Item` |
| 运行脚本 | `./script.sh` | `.\script.ps1` |

## Docker相关注意事项

### Docker Desktop for Windows

如果使用 Docker Desktop for Windows：

1. **共享驱动器设置**：确保在 Docker Desktop 设置中开启了文件共享
2. **路径转换**：Windows路径会自动转换为 Linux 风格
3. **性能提示**：项目放在 WSL2 文件系统中会更快

```powershell
# WSL2 中访问 Windows 路径
/mnt/c/Users/你的用户名/...
```

### Docker Compose

在 Windows 上运行：

```powershell
# 使用 PowerShell
cd C:\你的项目路径
docker-compose -f docker-compose-doris.yml up -d
```

## 常见问题

### Q: 如何知道我的 Windows 本地路径？

A: 如果你使用以下方式，通常可以在以下位置找到：

- **VS Code**: File → Open Folder，看你打开的是哪个目录
- **Git**: 在本地仓库运行 `git rev-parse --show-toplevel`
- **文件系统**: 通过文件管理器查看

### Q: 文件权限问题？

A: WSL2/Windows 中文件权限可能有问题，建议：
```powershell
# 在 Windows 上创建的文件，在 Linux 中可能需要
chmod +x scripts/*.sh
```
