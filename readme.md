# Final Project: MiniGit

Due: Thursday, June 6th, 11:59 PM  
200 points

## Overview

For the first assignment, you will implement XYZ

  **This assignment is an individual assignment.** You may ask Professors/TAs/Tutors for some guidance and help, but you can’t copy code. You may discuss the assignment conceptually with your classmates, including bugs that you ran into and how you fixed them. **However, do not look at or copy code.** This constitutes an Academic Integrity Violation.

### Contents
- [Part 1  - Introduction](#part-1---introduction)
- [Part 2 - Setup](#part-2---setup)
- [Part 3 - Implementation](#part-3---implementation)
- [Submission](#submission)

## **START EARLY!**

## Part 1 - Introduction
[(top)](#contents)

### Introduction to Git

[Link to tutorial](/dne)

### Introduction to Persistence

[Link to tutorial](/dne)

You may have noticed by now that you will need to a way to preserve the state of a program after it finishes running. 
The way this is accomplished is through the concept of **persistence**. For example, if your program writes contents to a file,
the file will stay there even after the program finishes running.

First, let's start with basic persistence: writing plain text to a file. 

[Writing to files]

[Reading from files]

[Managing directories]

Now, what happens if we wanted to store an entire object in a file? If we wanted to save a linked list, for example, maybe we could go through 
each node and save the data on a new line. Then we can reconstruct the original linked list in the future whenever we want by going through the 
file line by line and adding each element to a new linked list. However, this is inefficient and can quickly get very complicated for more advanced 
data structures. The concept of **serialization** comes to the rescue.

[Explain serializable]

[Explain write object]

[Explain read object]


## Part 2 - Setup
[(top)](#contents)

### Starter Code

Please follow the following steps to get the startercode:
1. Create a new repository on github
   - Make sure you DO NOT check off the option that says "Add a README file" 
2. Clone this empty repository to your system
3. Navigate to this repository from your terminal
4. Paste the following commands
  ```
  git remote add skeleton https://github.com/kalkulator413/dsc30-project1-startercode.git
  git pull skeleton main
  ```

Then, go to IntelliJ, press the "open a new folder" option, and select the repository you just cloned.

## Part 3 - Implementation
[(top)](#contents)

Write the following methods inside class `Repository`:

### init

```
public void init()
```
Initialize a minigit repository

### add

```
public void add(String[] args)
```
Stage files

### commit

```
public void commit(String[] args)
```

### rm

```
public void rm(String[] args)
```

### log
```
public void log()
```

### globalLog

```
public void globalLog()
```

### find

```
public void find(String[] args)
```

### checkout
```
public void checkout(String[] args)
```

### branch
```
public void branch(String[] args)
```

### rmBranch
```
public void rmBranch(String[] args)
```

### reset

```
public void reset(String[] args)
```

### status

```
public void status()
```

## Submission
[(top)](#contents)

Files to Submit
- HelloWorld.java
- ProgrammingChallenges.java
- OneHot.java (Extra Credit, Optional)

Instructions for Submission

![image](https://user-images.githubusercontent.com/50224596/231064671-3d79706b-29da-44ea-a238-b80dd9760757.png)

Once you finish your assignment, you should commit and push your code to Github first.

Then log into Gradescope, and select our course DSC30. On the class dashboard, select the current assignment PA1.

Upon clicking the assignment, a window will prompt for the submission method to select. Select **GitHub** as your submission method. (Note that we only keep the Upload submission method in the first PA in case you have problems using Github. **Starting from next PA, we will disable the upload method and leave only Github as the submission method**) Then click **Connect to GitHub**. Now log in to your Github account and click **Authorize gradescope**.

![image](https://user-images.githubusercontent.com/50224596/231064731-88ee3f59-f8d9-4955-a1ac-0ef27d5a3f54.png)

Select your private repository **dsc30-pa1** as the REPOSITORY, and **main** as the BRANCH. Then click Upload.

A notification window will display once your files have been submitted.

***IMPORTANT***: Please ensure that you see the following successful submission message under Autograder Results. If you see any other output, your submission is NOT successful. Check the submission error messages in the next section. If you need assistance or see anything unusual, please ask one of the staff or post on Piazza.
- Common mistake: Make sure that ProgrammingChallenges.java is located within the src folder.

![image](https://user-images.githubusercontent.com/50224596/231064811-963591a0-ba98-4fcc-abf8-897bd405e49c.png)

### Submission Error Messages
In a partial or incomplete submission you may receive the following error messages:
- SUBMISSION FAILED. THE FOLLOWING FILES DO NOT EXIST

  If you see this, make sure that you have submitted all of your .java files required for this assignment.

- SUBMISSION FAILED. FILES DO NOT COMPILE.

  Seeing this message means that there are compilation errors in your code. Check the error output and debug these errors before continuing.

- SUBMISSION FAILED. FILES DO NOT COMPILE WITH TESTING CODE.

  At least one of your method signatures is not consistent with the write-up. The error output will specify the lines corresponding to incorrect method signature.

Let us know if you see any other error messages.
