import os
import re

def remove_javadoc_comments_in_directory(root_dir):
    # Regex pattern to match multi-line  comments safely
    javadoc_pattern = re.compile(r'/\*\*.*?\*/', re.DOTALL)
    
    modified_files_count = 0

    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            file_path = os.path.join(dirpath, filename)
            
            try:
                # Read the file content
                with open(file_path, 'r', encoding='utf-8') as file:
                    content = file.read()
                
                # Find all matching comment blocks before replacing them
                matches = javadoc_pattern.findall(content)
                
                if matches:
                    print(f"\n[FILE FOUND] {file_path}")
                    for idx, comment in enumerate(matches, 1):
                        # Clean up formatting for the preview snippet
                        preview = " ".join(comment.split())
                        if len(preview) > 80:
                            preview = preview[:77] + "..."
                        print(f"  └─ Removed Comment #{idx}: {preview}")
                    
                    # Perform the replacement
                    new_content = javadoc_pattern.sub('', content)
                    
                    # Write back the cleaned content
                    with open(file_path, 'w', encoding='utf-8') as file:
                        file.write(new_content)
                        
                    modified_files_count += 1
                    
            except Exception as e:
                # Skip binary files or files that throw encoding errors
                pass

    print(f"\nDone! Successfully removed Javadoc comments across {modified_files_count} file(s).")

if __name__ == "__main__":
    target_directory = "."
    
    print(f"Scanning '{os.path.abspath(target_directory)}' for Javadoc comments...")
    remove_javadoc_comments_in_directory(target_directory)