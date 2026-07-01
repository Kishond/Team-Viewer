import os

# הגדרת תיקיית השורש של הפרויקט (התיקייה הנוכחית)
PROJECT_ROOT = '.'
# שם קובץ הפלט
OUTPUT_FILE = 'full_project_code.txt'
# תיקיות שנתעלם מהן (כדי למנוע זבל וקבצים מקומפלים)
IGNORE_DIRS = {'.git', '.gradle', 'build', '.idea', 'out', 'bin', 'node_modules', 'target'}
# סיומות קבצים שנרצה לכלול בסריקה
INCLUDE_EXTENSIONS = {'.java'}

def main():
    target_path_part = os.path.join('src', 'main', 'java')
    
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as outfile:
        for root, dirs, files in os.walk(PROJECT_ROOT):
            # נבדוק אם אנחנו נמצאים בתוך תיקיית src/main/java
            if target_path_part not in root:
                continue
                
            # סינון תיקיות לא רלוונטיות
            dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
            
            for file in files:
                extension = os.path.splitext(file)[1].lower()
                if extension in INCLUDE_EXTENSIONS:
                    file_path = os.path.join(root, file)
                    relative_path = os.path.relpath(file_path, PROJECT_ROOT)
                    
                    # לא נכתוב את קובץ הפלט לתוך עצמו
                    if relative_path == OUTPUT_FILE or file == 'export_project.py':
                        continue

                    outfile.write(f"\n{'='*80}\n")
                    outfile.write(f"FILE: {relative_path}\n")
                    outfile.write(f"{'='*80}\n\n")
                    
                    try:
                        with open(file_path, 'r', encoding='utf-8') as infile:
                            outfile.write(infile.read())
                    except Exception as e:
                        outfile.write(f"[Could not read file {relative_path}: {e}]\n")
                    outfile.write("\n")
    
    print(f"Success! The full project code has been consolidated into: {OUTPUT_FILE}")

if __name__ == "__main__":
    main()
