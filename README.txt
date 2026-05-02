# Mina Catherine Warner's 
CSE241 Final Project
[Repo Link]<https://github.com/mcw227/MCWInimicalDB.git>


## Introduction
Welcome to the README for Mina's Inimical database interface implementations. This terminal-based application is intended to function as a customer and managereal interface for a fictional restaurant called *Inimical's.* There are three main interfaces built so far: Customer, Location Manager and Sales Manager (Manager). The details of these interfaces, alongside other helpful or insightful information will be discussed down below.
Also, all of the transactions within the features are *hopefully* atomic. Otherwise... what would be the point of using a database, you know?

***IMPORTANT***
<br><br>***IMPORTANT***
In order to test the two manager interfaces, log in with **id = 2 for location manager** and **id = 3 for sales manager**. The rest of the employees are randomly defined. <br>**You may enter any number 1-100 for the customer database.**
Also, my ERD changed since I submitted my previous one. The new one is zipped into the file.

**TO RECOMPILE CODE**: `make run_jar`

## Table of Contents
* [Introduction](#introduction)
* [AI Use Disclosure](#ai-use-disclosure)
* [Mock Data Generation](#mock-data-generation)
* [SQL Extras](#sql-extras)
* [Customer Interface](#customer-interface)
* [Manager Interfaces](#manager-interfaces)
    * [Location Manager](#location-manager)
    * [Sales Manager](#sales-manager)
* [Business Decisions](#business-decisions)
* [Afterword](#afterword)
* [Key Take-Aways](#key-take-aways)
    

## AI Use Disclosure
Generally, I do avoid (to the best of my abilities.. you can't even search these days!) using any kind of AI with my projects because I feel it defeats the point of taking the course. You haven't really learned anything if you have to rely on a tool to do the job for you.<br>
BUT! I think that AI *should* be used as a powerful tool (alongside personal effort) to speed up project workflow and debugging. In this case, by "workflow", I mean parts of the project that are either strictly aesthetic (list pager class), unrelated to the content of this class (I forgot how method overriding vs overloading works... so I had to ask Gemini what the difference was...), or "outside" the scope of this class (e.g.: How to fetch generated ID from an update query in java; What is a good regex expression to capture emails?). <br>
That being said, there should be only one example of AI code that was copy-pasted into any of my files. That being Pager.java class. That class was originally written by hand (you may be able to see its many iterations through my github repo), but never worked quite right. I spent about three hours on it throughout the project, but I really just could not get it to work consistently. In the end, I decided my time was better spent elsewhere in the project and on code actually relevant to the course, so I resorted to Gemini for debugging and it told me that my math was not only unnecessarily complex, but also logically flawed... and then gave me a much shorter solution that worked 100% of the time.
Unfortunately I don't have access to the exact prompt & response anymore, but I believe it was something like this:<br>

1. "Why doesn't my paged list next function work?"
2. (Why my function is flawed, followed by working solution)
3. "Can you help me with my previous function too?"
4. (working previous function)

As for the other uses, these were the approximate questions I asked:
1. "What does [insert vaguely worded Oracle SQL error here] error mean?"
1. "What is the difference between method overriding and overloading super/subclass methods in Java?"
1. "If my ID is created with [insert automatic ID creation syntax here], how can I obtain the generated ID upon insertion using JDBC?"
1. "Can you generate a simple regex expression to verify emails?"

This concludes my AI usage disclosure.

## Mock Data Generation
Mock data was generated in part by "Mockaro" for customers, phone numbers and credit cards. I then got lazy... the rest of the data was generated using the python library called "faker." That being said, you may be wondering why certain numbers are much larger than they should be.
For example, tax at many locations exceeds 50%! Some items cost an insane amount of money! The reason for this is that I wanted to use large numbers so that it was easy to see things such as sales tax take effect as I was testing. Probably not the best method for that... but it led to some pretty funny pricing...

Within the "DBSetup" folder, there are three files:
1. table_dropper.sql (self explanatory)
2. table_maker.sql (self explanatory)
3. table_populator.py

The only one of note is table_populator.py which can be used to generate mock data that fits within the expectations of the CLI.
It is some *really* dodgy code, but it works! I would like to apologize to all of my comp. sci. professors for creating such a wretched collection of bad coding practices.

## File Details
&emsp;All java source files are located in mcw227/src. Almost all of them are POJOs that represent entities in the database. They are all... mostly... well commented.
&emsp;There is a makefile that can be used to re-compile the jar from that code as necessary. It is what I used to test my project throughout development

## SQL Extras
There are a number of extra views and triggers that I created in order to "make my life easier"... or so I said. The reality turned out not to be quite so kind... I'll never use triggers again! But-- here is a list of views and triggers I wrote:
**VIEWS**
1. all_items_class_view (for classifying items as "ingredient," "customer creation," or "signature")
1. customer_creations_view (class view)
1. signature_item_view (class view)
1. ingredients (class view)
1. order_customer_view (convenience)
1. order_item_view (convenience)
1. menu_item_view (convenience)
1. local_menu_view (convenience)
**TRIGGERS**
1. master_menu (adds all new items to the master menu with id = 1)
1. upd_customer_points (updates customer points based on order totals)
1. upd_item_price (updates the price of signature items... should have been a view)
1. upd_order_price (updates the price of orders based on their order items... should have been a view)

## Customer Interface
The customer interface is the most fleshed out of all the interfaces, and is also intended to be the most "convenient" to use. Unlike the other interfaces, I tried to make sure that *all* the necessary information was presented-- unlike in the managereal interfaces where I assume that the user will already know IDs or is at least okay with moving between windows to find them.

**FEATURES**
* ID Sign-in
* Name/Email Change
* Add Credit Cards and Phone Numbers
* Activate/Deactivate Membership
* Make/Check Orders
    * *It is during the order creation process one can make custom items*
* Deactivate Account

## Manager Interfaces
The next two interfaces are wrapped into the same "parent" interface since they both use the same "employee" table and ids to log into the platform. Then-- depending on the role the employee is assigned-- they will be logged into either the Local Manager Interface (role == 1) or the Sales Manager Interface (role == 2).

### Location Manager
The location manager interface is the second most powerful interface, and is intended to be used by managers who serve in-house at the *Inimical's* stores.

**FEATURES**
* View/Update The Status Of Orders
* View Items
    * *Intended so that they may view the ingredients of customer creations*
* View Locations
* View/Edit Local Menus
    * *They may only edit the menus of their own restaurant*
* Create Local Price Changes
    * *Only for their own restaurant*
* Create New Customer Account
* Re-Activate A Deactivated Customer Account

### Sales Manager
The sales manager account is the least powerful interface, with only two measly capabilities. It is mainly intended for a sales manager who wants to observe basic sales information about the locations and items.

**FEATURES**
* Check Location Sales Summary
* Check Item Sales Summary

## Business Decisions
There were a few important business decisions I made throughout the project, either due to accidental technical restraints I imposed on myself, or as an adaptation to flexibile project description. The larger business decisions are discussed here. (The smaller ones I forgot)
1. **Customers have to ask managers to create accounts at restaurants**
    * Inimical's is a very tight knit community. They only let outsiders in after they visit the in person restaurant a few times and take part in some sort of... "ritual"?
1. **There is a Membership Program**
    * Customers may sign up for a membership in order to accrue points, rather than simply adding a card to their account. It seems that with the benefit of "frequent eater" points, also comes the assumption that one siphons some of their soul to the dark realm daily. That's the only way they can make up for the financial loss...
    <br>*This change was made because the interface forces customers to add a card under their account.*
1. **Customers May Only Create Items In The Order Screen**
    * Due to differing prices and availability of ingredients at different locations, *Inimical's* customer creations cannot be made until a location is chosen on the order screen, in order to make sure the price changes and availability of ingredients at those locations is accounted for.
    * Also, customer creations are not automatically made available at all locations (due to aforementioned ingredient availability differences). Instead, they are added to a "Master Menu" that the Location Manager may use to migrate customer creations over to their local menus.
1. **Customer Accounts Cannot Be Deleted, Only Marked As Inactive**
    * *Inimical's*, like many other customer database users, does not delete customer accounts until strictly necessary for record keeping purposes
    *This is also true for credit cards... they definitely aren't stealing your credit card info!*

## Afterword
I definitely learned a lot through doing this project. Most notably.. I learned just how hard it is to build a good database front-end. I had to refactor my code-base at least three times... and it still isn't even that nice to use... 
Then there was the database itself! I ended up revising my ERD at least a two more times after I submitted the "final" iteration. I also went ahead and created views and triggers... sort of wherever I could put them-- and as a result I ended up shooting myself in the foot later on. All of my triggers (which should have all been view, by the way!!) cause major deadlock issues when deleting items. So, I had to create systems that worked around it. However, I will say it was at least mildly amusing to see the issues of concurrency that we discussed in class in action... Though I wish it wasn't in *my* code...
Hey! At least I won't have any issues dealing with concurrency questions on the final!
Speaking of concurrency... For some reason I thought that Java garbage collection would automatically close my PreparedStatements when they went out of scope... So I completely forgot to use "try-with-resources" statements on **EVERY SINGLE ONE** of them... I had to spend like... an hour fixing that.

## Key Take-Aways
1. Always use views instead of triggers where possible
2. Develop records/POJOs for Database entities early on to avoid refactoring
3. When creating the ERD, *really* make sure that you know all the requirements and have a clear view of the scenario in mind.
4. Always make sure you close PreparedStatements/ResultSets
