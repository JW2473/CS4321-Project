# CS4321-Project2

## Interpreter
The top level of our program is client.Interpreter, and the entry is the main function of it. The interpreter get the data and querys from the input path and output the results to ouptput path.
## Logical Operator and Physical Operator
you can find the logical operators in the package called "logicaloperators" and the pysical operators in the package called "physicaloperators" under src.
## Physical Plan Builder
you can find that there is class called "PhysicalPlanBuilder" under the package visitors and that's it.
## Partition Reset Logic
The tuple reader creates an array during initialization. It is used to keep track of the accumulated number of tuples when a new page is read. The reset method takes one parameter which is the index of the tuple in the relation. By using binary search in the array, we can easily find out which specific page that index is in. Then we set the file channel to that page and reset the array to make it only contain the leading pages. Then we read the page into the buffer. The bytes in the buffer can be calculated by substracting the index from the acuumulated number of tuples and multiplied by the byte length of each tuple and plus the byte length of the metadata. Finally, we set the buffer to that position. When we get next tuple, we can get the tuples starting from that index.
