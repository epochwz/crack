# Crack Markdown Navigator

## Usage

1. 备份 `C:/Users/epoch/.IntelliJIdea2017.3/config/plugins/idea-multimarkdown/lib/idea-multimarkdown.jar`
2. 替换
   - 使用 `7z` 等压缩工具打开 `idea-multimarkdown.jar`
   - 复制本项目中的 `libs/2.9.0/LicenseAgent.class` 到 `idea-multimarkdown.jar` 中的 `/com/vladsch/idea/multimarkdown/license/LicenseAgent.class`
3. 重启 IDEA

## Tutorial

1. 使用 IDEA 新建 Java 项目 `CrackMarkdownNavigator`
2. 从 `idea-multimarkdown.jar` 中复制源文件 `/com/vladsch/idea/multimarkdown/license/LicenseAgent.java` 到项目 `/src/com/vladsch/idea/multimarkdown/license/LicenseAgent.java` 中
3. 添加项目依赖
   - `C:/Program Files/JetBrains/IntelliJ IDEA 2017.3.6/lib`
   - `C:/Users/epoch/.IntelliJIdea2017.3/config/plugins/idea-multimarkdown/lib`
4. **破解**：修改源代码 -- 参考代码提交记录
5. 编译生成新的 `LicenseAgent.class` 文件：运行一下 main 方法即可
6. 备份 `C:/Users/epoch/.IntelliJIdea2017.3/config/plugins/idea-multimarkdown/lib/idea-multimarkdown.jar`
7. 替换
   - 使用 `7z` 等压缩工具打开 `idea-multimarkdown.jar`
   - 复制本项目中的 `libs/2.9.0/LicenseAgent.class` 到 `idea-multimarkdown.jar` 中的 `/com/vladsch/idea/multimarkdown/license/LicenseAgent.class`
8. 重启 IDEA
