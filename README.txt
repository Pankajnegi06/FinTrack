FinTrack - Expense & Budget Tracking Application

Compile (from the FinTrack folder):
  javac -d out $(find src -name "*.java")        (Linux/Mac)
  dir /s /b src\*.java > sources.txt & javac -d out @sources.txt     (Windows)

Run:
  java -cp out com.fintrack.ui.Main

Data is saved in expenses.txt and budget.txt in the folder you run from.
In IntelliJ/Eclipse: mark src as Sources Root, test as Test Sources Root, add JUnit 5, run Main.java.
