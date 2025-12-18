import re
import sys
import os

def resolve_file(file_path):
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Pattern to find conflict blocks
    # We assume standard git conflict markers
    # <<<<<<< Updated upstream
    # ...
    # =======
    # ...
    # >>>>>>> Stashed changes
    
    # We want to keep the content between ======= and >>>>>>> Stashed changes
    
    # Regex:
    # <<<<<<< Updated upstream\n(.*?)\n=======\n(.*?)\n>>>>>>> Stashed changes
    # But we need to handle variations in whitespace and newlines.
    
    pattern = re.compile(r'<<<<<<< Updated upstream(.*?)=======(.*?)>>>>>>> Stashed changes', re.DOTALL)
    
    def replacer(match):
        # match.group(1) is Upstream (reject)
        # match.group(2) is Stashed (keep)
        return match.group(2)
    
    new_content = pattern.sub(replacer, content)
    
    if new_content != content:
        print(f"Resolved conflicts in {file_path}")
        with open(file_path, 'w') as f:
            f.write(new_content)
    else:
        print(f"No conflicts found matching pattern in {file_path}")

if __name__ == "__main__":
    files = sys.argv[1:]
    for f in files:
        if os.path.exists(f):
            resolve_file(f)
        else:
            print(f"File not found: {f}")
