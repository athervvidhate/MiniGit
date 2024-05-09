# Final Project: MiniGit

Due: Thursday, June 6th, 11:59 PM  
200 points

## Overview

For the first assignment, you will implement XYZ

  **This assignment is an individual assignment.** You may ask Professors/TAs/Tutors for some guidance and help, but you can’t copy code. You may discuss the assignment conceptually with your classmates, including bugs that you ran into and how you fixed them. **However, do not look at or copy code.** This constitutes an Academic Integrity Violation.

Contents
- [Part 1  - Introduction](#part-1---introduction)
- [Part 2 - Setup](#part-2---setup)
- [Part 3 - Implementation](#part-3---implementation)
- [Submission](#submission)

## **START EARLY!**

## Part 1 - Introduction

## Part 2 - Setup

### Starter Code

[Starter Code can be found at this link](https://github.com/ucsd-dsc30/sp23-startercode). You can use the git skills you just learned to clone this repository into your own computer. You’ll clone the starter code repository first, and copy ProgrammingChallenges.java (in the cloned repo) into the src folder of the IntelliJ project - the same location where `HelloWorld.java` is created. Note that you should not clone the starter code repository within your IntelliJ project nor within the dsc30-pa1 repo.

Alternatively, for PA1 specifically you can [access the StarterCode at this link](https://drive.google.com/drive/folders/1lLhd9hN2Y_2sre3dbpz6fuH-oayM1Bjq). For future PAs you will be required to fetch the StarterCode at the GitHub repository.

## Part 3 - Implementation

Write the following methods inside class `ProgrammingChallenges`:

> Problem 1 (Purpose: Boolean Operators)

```
// 1
public static boolean store(String item, float inWallet, float needed)
```

You decided to celebrate the beginning of the quarter and went to the store to buy something tasty. You have some cash in your wallet and there is also an amount that you must pay at the store. Write a method that returns true when the item is equal to “cake”, “ice-cream”, or “sushi” and you have enough money in your wallet.

**Example: (notice how we denote a float here)**

```
Input: sushi, 4.4f, 5.43f
Output: false  

Input: rice, 100f, 5f
Output: false  

Input: ice-cream, 100f, 5f 
Output: true  

Input: ice-cream, 5f, 10.5f 
Output: false
```

> Problem 2. (Array manipulations, no loop)

```
// 2
public static boolean compareArrays(int [] arr1, int [] arr2)
```

Given two integer arrays, with length 1 or more. Write a method that returns true when:
- Both arrays have different lengths and
- The first element of the first array is the same as the last element of the second array and
- The last element of the first array is the same as the first element of the second array.

Return false otherwise.

**Example:**

```
Input: [1, 3, 4], [2, 4, 6] 
Output: false  

Input: [1, 3, 4], [4, 4, 6, 1]
Output: true

Input: [1, 3, 4], [4, 4, 1]
Output: false
```

> Problem 3. (Purpose: Single for loop and array practice)

```
// 3
public static int countNumbers(int [] arr1)
```

Given one integer array. Write a method that counts:
- The number of even numbers NOT divisible by 3.

**Example:**

```
Input: [1, 3, 4] 
Output: 1  

Input: [4, 4, 6, 1]
Output: 2

Input: [6, 12, 18]
Output: 0
```

> Problem 4. (Purpose: Single while loop)

```
// 4 
public static float [] positiveAverage(int [] arr1)
```

Given one integer array. Write a method that calculates:
- The number of positive integers in a given array
- The average of these positive integers.

Returns both numbers in an array, where the first element is the number of positive integers, and the second element is their average.

**Notes:**
1. Round the average answer to 2 decimals.
2. Use `Math.round()` for rounding. However, this method only returns an integer! Try to figure out a way around.
3. Try to use a while loop for practice to solve this problem.

**Example**:

```
Input: [1, 3, 4] 
Output: 3.0
      2.67
  
Input: [4, 4, -6, 1]
Output: 3.0
       3.0

Input: []
Output: 0.0
      0.0

Input: [-4, -4, -6, -1]
Output: 0.0
      0.0
```

> Problem 5. (Purpose: Combining a while loop and Boolean logic)

```
// 5
public static boolean sameDigitFirstAndLast(int num1, int num2)
```

Write a method that takes two non-negative integers and returns `true` if the first digit of the first number is the same as the last digit of the second number.

**Example:**

```
Input: 57, 78 
Output: false  

Input: 7333, 7
Output: true
```

> Problem 6. (Purpose: Combining a for loop and Boolean logic)

```
// 6 
public static boolean decreasingOrder(int[] elems)
```

Write a method that takes in an integer array and returns `true` if there are **three** consecutive decreasing numbers in this array.

**Example:**

```
Input: [1, 2, 3, 6, 5, 4]
Output: true
Explanation: 6, 5, 4 are decreasing. 

Input: [5, 6, 7, 10, 6, 3]
Output: true
Explanation: 10, 6, 3 are decreasing. 

Input: [10, 8, 22, 21, 30, 29] 
Output: false 

Input: [6, 3, 5, 2, 4, 1]
Output: false
Explanation: numbers are not in consecutive order
```

> Problem 7. (Purpose: Manipulating a 2-D Array)

```
// 7 
public static void replaceMainDiagonal(int[][] elems)
```

Write a method that takes a 2D integer array (square matrix) and replaces its main diagonal with its sum by mutating the input.

> Problem 8. (Purpose: Loop and Array)

```
// 8
public static float averageGrade(int[][] grades, int assignmentIndex)
```

Write a method that takes in a 2-D array where each row represents a student and each column represents an assignment, and the index for an assignment. It returns the average grade for an assignment at the given index.

**Notes:**
1. Round the answer to 3 decimals.
2. Use `Math.round()` for rounding. However, this method only returns an integer! Try to figure out a way around.
3. Assume the assignmentIndex will be valid.


**Example:**

```
Input: [[1, 3, 2, 2], [2, 3, 4, 3], [9, 9, 10, 10]], 0
Output: 4.0
Explanation: (1+2+9)/3 = 4.0
	
Input: [[1, 10, 3, 2, 7], [10, 4, 3, 4, 3], [6, 3, 9, 1, 3]], 1
Output: 5.667
Explanation: (10+4+3)/3 = 5.667
```


> Problem 9. (Purpose: Recursion and String Operations)

```
// 9
public static String noDots(String str)
```

Write a method that takes in a string and returns a new string where all the dots ('.') have been removed.

**Notes:**
1. This method should be done by **recursion**. (There is a one line solution that uses the `replace` method, you are NOT allowed to use it).
2. Built-in methods `charAt` and `substring` can be useful here.

**Example:**

```
Input: "Recursion is cool." 
Output: "Recursion is cool"

Input: "M.A.R.I.N.A"
Output: "MARINA"

Input: "no dots!"
Output: "no dots!"
```

> Problem 10. (Purpose: Conditionals and Arrays)

```
// 10
public static int[] twoElements(int[] elems)
```

Write a method that takes in an array of integers and returns a new array of length 2 containing two elements:
- If the original array's length is less than two, return an empty array.
- If the original array's length is even, return the middle two elements from the original array
  0 If the original array's length is odd, return the first and the last elements as the output array.

**Requirement:**
- Your function should not modify the original array.

**Example:**

```
Input: [2, 2, 3, 3, 4, 4, 5, 5] 
Output: [3, 4]  

Input: [3] 
Output: [] 

Input: [1, 14, 3] 
Output: [1, 3] 
```


## Submission

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
