# CS4321-Project1

## Interpreter
The top level of our program is client.Interpreter, and the entry is the main function of it. The interpreter get the data and querys from the input path and output the results to ouptput path.
## Join Logic
First, we get the fromItems from the query and put them into a list from left to right. Then we split the expressions from where clause and get the related tables for each expression. For the select conditions, the expression only relate to one table and for the join condition, it relates to two tables. We put the select condtion expression and its corresponding table into a select condition map. And we put the table and the join expression with its left tables into a join condition map.
To build the tree, we join the table from left to right. For each table, we first check if it has select condition expression, if it has, we first select the table, then we join the table to its left tables. Finally, we check if it needs to be sorted ot delete the duplicate tuples.
