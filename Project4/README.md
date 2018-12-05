# CS4321-Project

## Interpreter
The top level of our program is client.Interpreter, and the entry is the main function of it. The interpreter get the data and querys from the input path and output the results to ouptput path.

## Selection push logic
For pushing the selection, we used a union-find data structure to group attributes with same range condition. We created a visitor walk through the WHERE clause and pick all usable comparasion operator, merge them and update their numeric constraints. For all unusable comparasions, we put these expressions in a list for later use when joining them
## Selection implementation logic
For a specific table, it may have a lot of attributes involved in the selection. First, we pick all different attributes and check whether they have indexing information. If they don't have index, the cost is just all the pages in the table file. If they have index, for clustered, the cost is the fraction of all the data pages as determined by the reduction factor. For unclustered index, in the worst case, we have to fetch a page for every tuple, so the cost is reduction factor times leaf pages and tuple number. After we get all the cost, we can pick the lowest cost and scan the table using that attribute with proper scan operator so that we can get the smallest table size before we finally use a normal selection to select the tuples that meet all conditions. 
## Join order logic
First we calculate the size of each tables after selection. Then we apply dynamic programming to traverse all the order of join, and we record the minimum cost and its corresponding join order. The cost are the sum of each join size. At first, the cost are the table tuple number after selection. The intermediate join cost are the multiply of left join and the current table size, then divided by the max range of attribute contained in the join expression.
## Join implementation logic
If join expression have other-than-equality comparisons or the join is a pure cross products, we appply BNLJ, otherwise we apply SMJ.
## Known Bugs
1. The table names, column names and alias names in the query are case sensitive.
2. All the contents in temp directory must be delected before running all queries again
