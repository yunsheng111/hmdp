#!/usr/bin/env python3
"""
批量更新HTML文件，添加新的CSS和JS引用
"""

import os
import re
import glob

def update_html_file(file_path):
    """更新单个HTML文件"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # 1. 添加loading.css引用（如果还没有）
        if 'loading.css' not in content:
            # 在main.css后面添加loading.css
            content = re.sub(
                r'(<link href="\.\/css\/main\.css" rel="stylesheet">)',
                r'\1\n  <link href="./css/loading.css" rel="stylesheet">',
                content
            )
        
        # 2. 添加JS文件引用（在element.js后面）
        if 'loading.js' not in content:
            # 查找element.js的位置，在其后添加新的JS文件
            element_js_pattern = r'(<script src="\.\/js\/element\.js"></script>)'
            if re.search(element_js_pattern, content):
                replacement = (
                    r'\1\n<script src="./js/loading.js"></script>'
                    r'\n<script src="./js/message.js"></script>'
                    r'\n<script src="./js/performance.js"></script>'
                )
                content = re.sub(element_js_pattern, replacement, content)
        
        # 3. 确保common.js在最后加载（如果存在）
        if 'common.js' not in content and 'axios.min.js' in content:
            # 在axios.min.js后面添加common.js
            content = re.sub(
                r'(<script src="\.\/js\/axios\.min\.js"></script>)',
                r'\1\n<script src="./js/common.js"></script>',
                content
            )
        
        # 4. 更新页面跳转逻辑（如果有location.href）
        # 替换简单的location.href为util.navigateTo
        location_href_pattern = r'location\.href\s*=\s*["\']([^"\']+)["\'];'
        def replace_location_href(match):
            url = match.group(1)
            return f'''if (window.util && window.util.navigateTo) {{
          window.util.navigateTo("{url}");
        }} else {{
          location.href = "{url}";
        }}'''
        
        content = re.sub(location_href_pattern, replace_location_href, content)
        
        # 只有内容发生变化时才写入文件
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ 已更新: {file_path}")
            return True
        else:
            print(f"- 无需更新: {file_path}")
            return False
            
    except Exception as e:
        print(f"✗ 更新失败 {file_path}: {e}")
        return False

def main():
    """主函数"""
    # HTML文件路径
    html_dir = "hmdp-front/nginx-1.18.0/html/hmdp"
    
    if not os.path.exists(html_dir):
        print(f"错误: 目录不存在 {html_dir}")
        return
    
    # 查找所有HTML文件
    html_files = glob.glob(os.path.join(html_dir, "*.html"))
    
    if not html_files:
        print(f"在 {html_dir} 中没有找到HTML文件")
        return
    
    print(f"找到 {len(html_files)} 个HTML文件")
    print("开始更新...")
    
    updated_count = 0
    for html_file in html_files:
        if update_html_file(html_file):
            updated_count += 1
    
    print(f"\n更新完成! 共更新了 {updated_count} 个文件")

if __name__ == "__main__":
    main()
