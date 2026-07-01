 שלב 1: דרישות קדם והתקנות!!!!
!כדי להריץ את הפרויקט על מחשב חדש, יש לוודא שהתוכנות הבאות מותקנות:

התקנת Java (JDK):

יש להוריד ולהתקין את סביבת ההרצה של ג'אווה (מומלץ JDK 17 או JDK 21). ניתן להוריד מאתר Oracle או Adoptium.
חשוב: במהלך ההתקנה, ודא שמסומנת האפשרות להגדיר את משתני הסביבה (JAVA_HOME ו-PATH), כדי שמערכת ההפעלה תזהה את פקודות ה-Java.
סביבת פיתוח (IDE):

מומלץ להשתמש ב-VS Code (יחד עם התוסף "Extension Pack for Java") או ב-IntelliJ IDEA, מה שיהפוך את הרצת הקבצים השונים לפשוטה מאוד בלחיצת כפתור.
הורדת הפרויקט:

העתק את כל קובצי הפרויקט למחשב שלך או בצע שיבוט (Clone) דרך Git.
 שלב 2: אתחול הפרויקט
הפרויקט שלך משתמש ב-Gradle לניהול חבילות ותלויות (כמו ספריית ההצפנה או ספריות אחרות אם קיימות).

פתח את תיקיית הפרויקט המרכזית (Team-Viewer) בתוך סביבת הפיתוח (VS Code או IntelliJ).
המתן מספר דקות. ה-IDE אמור לזהות שמדובר בפרויקט Gradle ולהוריד אוטומטית את התלויות הדרושות.
כדי לוודא שהפרויקט מתקמפל בהצלחה, תוכל לפתוח טרמינל (Terminal) בתוך תיקיית הפרויקט ולהריץ:
במערכת Windows: gradlew.bat build
במערכת Mac/Linux: ./gradlew build
שלב 3: סדר ההרצה של המערכת
המערכת מורכבת מ-3 תוכניות שונות. מכיוון שהמארח והצופה תלויים בשרת כדי לתקשר, חובה להריץ אותם בדיוק בסדר הבא: (ההגדרות הנוכחיות בקוד מכוונות את כולם להתחבר לכתובת המקומית 127.0.0.1 בפורט 5000)

הפעלת שרת התיווך (Relay Server):

נווט לקובץ: server/RelayServer.java.
לחץ על כפתור ה-Run (או הרץ את פונקציית ה-main).
בטרמינל תראה הודעה: server has started ואחריה The server is litening on port: 5000.
הפעלת המחשב המארח (Host - המחשב שאליו נתחבר):

נווט לקובץ: host/HostManager.java.
לחץ על Run.
ייפתח חלון ממשק קטן (HostUI) שיבקש ממך לבחור מפתח התחברות.
הפעלת המחשב הצופה (Viewer - המחשב שישלוט):

נווט לקובץ: viewer/ViewerManager.java.
לחץ על Run.
ייפתח חלון של ה-Viewer שיבקש ממך להזין קוד.
🎮 שלב 4: מדריך שימוש בסיסי (איך להתחבר ולשלוט)
לאחר שכל שלושת הרכיבים פועלים ברקע:

בצד המארח (Host):

בחלון שקפץ, הקלד קוד סודי כלשהו (למשל: Secret123) ולחץ אישור.
הסטטוס בחלון המארח ישתנה ל-"Status: Waiting for Viewer..." והוא ימתין לחיבור.
בצד הצופה (Viewer):

בחלון שקפץ, הקלד את אותו הקוד בדיוק (Secret123) ולחץ אישור.
התחברות:

השרת יזהה ששני הקודים תואמים, ויקים ביניהם "ערוץ תקשורת פעיל" (Active Session).
הקוד שהוזן יהפוך גם למפתח ההצפנה (AES) לאבטחת התקשורת שלכם!
שליטה מרחוק:

מסך הצופה (ViewerUI) יציג כעת את מסך המארח בשידור חי (ויעדכן את התצוגה כ-30 פעם בשנייה).
כל לחיצת עכבר, גלילה או הקלדה במקלדת בתוך חלון ה-Viewer תועבר באופן מיידי ותבוצע על המחשב המארח בזכות מחלקת ה-Robot.
ניתוק מסודר:

כדי לסיים את החיבור בצורה בטוחה, המארח יכול ללחוץ על כפתור ה-"Stop Sharing" בממשק שלו, או לחלופין הצופה יכול פשוט לסגור את החלון שלו באמצעות ה-X (מה שישלח חבילת QUIT מסודרת ויסגור את התהליכים בשני הצדדים).

## 📚 ביבליוגרפיה (מקורות מידע - APA)

*   Google. (n.d.). *Guava: Google core libraries for Java*. GitHub. https://github.com/google/guava
*   Gordijn, M. (n.d.). *UCanAccess: A pure Java JDBC driver for Microsoft Access*. SourceForge. http://ucanaccess.sourceforge.net/site.html
*   National Institute of Standards and Technology. (2001). *Advanced Encryption Standard (AES)* (Federal Information Processing Standards Publication 197). U.S. Department of Commerce. https://doi.org/10.6028/NIST.FIPS.197
*   Oracle. (n.d.-a). *Class Robot (Java Platform SE 21)*. Oracle Help Center. https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/Robot.html
*   Oracle. (n.d.-b). *Java Cryptography Architecture (JCA) reference guide*. Oracle Help Center. https://docs.oracle.com/en/java/javase/21/security/java-cryptography-architecture-jca-reference-guide.html
*   Oracle. (n.d.-c). *Lesson: All about sockets*. The Java™ Tutorials. https://docs.oracle.com/javase/tutorial/networking/sockets/index.html
*   Oracle. (n.d.-d). *Writing Image I/O applications*. Oracle Help Center. https://docs.oracle.com/javase/8/docs/technotes/guides/imageio/spec/apps.gui.html
*   The JUnit Team. (n.d.). *JUnit 5 User Guide*. JUnit.org. https://junit.org/junit5/docs/current/user-guide/
