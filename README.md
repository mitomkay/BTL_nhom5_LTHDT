# BankAccountPrj

Java console application: Bank Account Management System (Topic 9).

## Structure
```
BankAccountPrj/
├── src/                 AccountType, BankAccount, AccountTypeList, BankAccountList,
│                        Menu, BankAccountManager (.java)
├── accounttypes.txt     sample data
├── bankaccounts.txt     sample data
└── UML_ClassDiagram.mermaid
```

## Compile and run (JDK 8+)
Run from the BankAccountPrj folder, because the data files are read from the current directory.
```
mkdir out
javac -encoding UTF-8 -d out src/*.java
java -cp out BankAccountManager
```
In an IDE (IntelliJ / Eclipse / NetBeans): create project `BankAccountPrj`, copy the
`.java` files into `src`, and put the two `.txt` files in the project root folder.

## Notes
* accounttypes.txt is loaded before bankaccounts.txt so each BankAccount can resolve its AccountType.
* Exit (0) never overwrites the files; use options 5 and 11 to save.
* Source files contain Vietnamese comments: save/open as UTF-8 and compile with `-encoding UTF-8`.
* Values must not contain `,` (it is the field separator in the files).
