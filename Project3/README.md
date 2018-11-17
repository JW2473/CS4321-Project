# CS4321-Project3

## Interpreter
The top level of our program is client.Interpreter, and the entry is the main function of it. The interpreter get the data and querys from the input path and output the results to ouptput path.

## Index Scan Operator
### ***lowkey*** and ***highkey***
The ***lowkey*** and ***highkey*** are set in the index scan operator and are passed into the tree reader for fetching the Rid. If any of the ***lowkey*** or ***highkey*** is **null**, the key will be set to `Integer.MIN_VALUE` or `Integer.MAX_VALUE`.
### Clustered and unclustered
Clustered and unclustered indexes are handled in index scan operator, tree reader and tuple reader. If the index is clustered, the tree reader will just return the first Rid that matches the ***lowkey***. The ***lowkey*** is used to reset the tuple reader. Then the ***highkey*** is passed to the tuple reader which scan all the tuples from the given Rid and return next tuple until it reaches the ***highkey***. For the unclustered index, the tree reader will return all the Rid between two keys and tuple reader will fetch specified tuple according to the given Rid.
### Deserialize the tree
To deserialize the tree, the tree reader recursively read index pages from the root page. It deserializes the index page and compare the given key to all the keys stored in that page. When it reaches the leaf page. It traverse to the ***lowkey*** and return all the Rid between ***lowkey*** and ***highkey***.
## Logical / Physical Plan Builder
The logic of how to decide to use index scan is defined in Physical Plan Builder. First, we check if both of the table and its corresponding attribute are defined in the index_info.txt. Then we parse the selection expression of the table to get the high key and low key for index scan. Finally, we check if the low key and high make sense like the low key is minor than the high key. If not, we apply full scan.
## Known Bugs
1. The table names, column names and alias names in the query are case sensitive.
