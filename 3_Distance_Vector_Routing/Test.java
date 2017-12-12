import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.io.IOException;


public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		//Take in inputs:
		Scanner reader = new Scanner(System.in);
		
		System.out.println("Enter your first command:  ");
		String filename1 = reader.nextLine();
		File file1 = new File(filename1);
		
		System.out.println("Enter your second command:  ");
		File file2 = new File(reader.nextLine());
		
		System.out.println("Enter your third command(enter 1 to get a detailed report):  ");
		
		int flag = Integer.parseInt(reader.nextLine());

		reader.close();
		
		//Prepare buffers
		BufferedReader buffer = new BufferedReader(new FileReader(file1));
			
		//Mode: Basic
		boolean keepRunning = true;
		String line = buffer.readLine();
		int total = Integer.parseInt(line) + 1;
		Router[] routers = new Router[total];
		PrintWriter writer = new PrintWriter("Output_basic.txt","UTF-8");
		//Construct n number of routers
		for (int i = 1; i < total; i++) {
			routers[i] = new Router(i, total);
		}	
		//add all the edges between
		while ((line = buffer.readLine()) != null) {
	        String[] input = line.split(" ");
	        int num1 = Integer.parseInt(input[0]);
	        int num2 = Integer.parseInt(input[1]);
	        int length = Integer.parseInt(input[2]);	        
	        Router r1 = routers[num1];
	        Router r2 = routers[num2];
	        r1.addNeighbors(num2, length);
	        r2.addNeighbors(num1, length);
	        
		}
		buffer.close();
		
		BufferedReader buffer2 = new BufferedReader(new FileReader(file2));
		
		//integer = iteration  arraylist = updated edges
		HashMap<Integer, ArrayList<Integer[]>> topChange = new HashMap<>();
		String line1 = "";
		int lastTop = 0;
		//add all the topological changes to the hashmap
		while ((line1 = buffer2.readLine()) != null) {
	        String[] input = line1.split(" ");
	        
	        int round = Integer.parseInt(input[0]);
	        lastTop = round;
	        int num1 = Integer.parseInt(input[1]);
	        int num2 = Integer.parseInt(input[2]);
	        int length = Integer.parseInt(input[3]);
	        
	        Integer[] tuple = new Integer[] {num1, num2, length};
	        
	        ArrayList<Integer[]> list = topChange.get(round);
	        if (list == null) {
	        	list = new ArrayList<Integer[]>();
	        }
	        list.add(tuple);
	        topChange.put(round, list);
	        
		}
		buffer2.close();
		
		System.out.println("Round 1" );
		if (flag == 1) writer.println("Round 1" );
		for (int i = 1; i < total; i++) {
			//after adding all the edges in, initialize the table
			routers[i].initialize();
			Pair[][] pair = routers[i].getTable();
			for (int j = 0; j < total; j++) {
				if (j == 0) {
					System.out.print(i + " ");
					if (flag == 1) writer.print(i + "\t");
				} else if (pair[i][j] == null) {
					System.out.print("-1,-1" + "\t");
					if (flag == 1) writer.print("-1,-1" + " \t");
				} else {
					System.out.print(pair[i][j] + "\t");
					if (flag == 1) writer.print(pair[i][j] + " \t");
				}
			}
			System.out.println();
			if (flag == 1) writer.println();
			
		}
		int iterations = 1;
		boolean converged = false;
		
		while (!converged || iterations < lastTop) {
			converged = true;
			iterations++;
			
			//check topological change in this iteration
			ArrayList<Integer[]> event = topChange.get(iterations);
			
			if (event != null) {
				System.out.println("some edges are updated");
				for (Integer[] tuple : event) {
					routers[tuple[0]].edgeUpdate(tuple[1], tuple[2]);
					routers[tuple[1]].edgeUpdate(tuple[0], tuple[2]);
				}
			}
			
			System.out.println("Round: " + iterations);

			//each router get distance vectors from all it's neighbors
			for (int r = 1; r < total; r++) {
				Router receiver = routers[r];
				Set<Integer> neighbor = receiver.getNeighbors().keySet();
				//get vectors from neighbor
				for (Integer n : neighbor) {
					Pair[] vector = routers[n].getBasic(r);
					receiver.receive(n, vector);
				}	
			}
			
			//perform update on each routers
			for (int r = 1; r < total; r++) {
				boolean changed = routers[r].run();  //indicated whether an update occured
				converged = converged && !changed;
			}
			
			for (int index = 1; index < total; index++) {
				Pair[][] pair = routers[index].getTable();
				for (int j = 0; j < total; j++) {
					if (j == 0 && keepRunning) {
						System.out.print(index + " ");
					} else if (keepRunning) {
						if (pair[index][j] == null) {
							System.out.print(pair[index][j] + "\t" );
						} else if (pair[index][j].gethopCount() > 100) {
							String msg = "The program has encountered a Count-to-infinity instability.";
							System.out.println("\n" + msg);
							writer.println(msg);
							writer.close();
							keepRunning = false;
							converged = true;
						} else {
							System.out.print(pair[index][j] + "\t" );
						}
					}
					
				}
				System.out.println("");
			}
			if (flag == 1 && keepRunning) {
				writer.println("Round " + iterations);
				for (int index = 1; index < total; index++) {
					Pair[][] pair = routers[index].getTable();
					for (int j = 0; j < total; j++) {
						if (j == 0) {
							writer.print(index + "\t");
						} else if (pair[index][j] == null) {
							writer.print("-1,-1" + " \t" );
						} else {
							writer.print(pair[index][j] + " \t" );
						}
						
					}
					writer.println("");
				}
			}
		}
		
		if (flag == 0 && keepRunning) {
			for (int index = 1; index < total; index++) {
				Pair[][] pair = routers[index].getTable();
				for (int j = 0; j < total; j++) {
					if (j == 0) {
						writer.print(index + "\t");
					} else if (pair[index][j] == null) {
						writer.print("-1,-1" + " \t" );
					} else {
						writer.print(pair[index][j] + " \t" );
					}
					
				}
				writer.println("");
			}
		}
		if (keepRunning) {
			System.out.printf("\nConvergence delay: %d round\n\n", iterations - lastTop);
			writer.println("");
			writer.printf("\nConvergence delay: %d round\n\n", iterations - lastTop);
			writer.close();
		}
		
		
		//Mode: Split Horizon
		keepRunning = true;
		BufferedReader buffer_split = new BufferedReader(new FileReader(file1));
		line = buffer_split.readLine();
		total = Integer.parseInt(line) + 1;
		routers = new Router[total];
		PrintWriter writer2 = new PrintWriter("Output_split.txt","UTF-8");
		//Construct n number of routers
		for (int i = 1; i < total; i++) {
			routers[i] = new Router(i, total);
		}	
		//add all the edges between
		while ((line = buffer_split.readLine()) != null) {
	        String[] input = line.split(" ");
	        int num1 = Integer.parseInt(input[0]);
	        int num2 = Integer.parseInt(input[1]);
	        int length = Integer.parseInt(input[2]);

	        Router r1 = routers[num1];
	        Router r2 = routers[num2];
	        r1.addNeighbors(num2, length);
	        r2.addNeighbors(num1, length);
	        
		}
		buffer_split.close();
		
		BufferedReader buffer2_split = new BufferedReader(new FileReader(file2));
		
		//integer = iteration  arraylist = updated edges
		topChange = new HashMap<>();
		line1 = "";
		lastTop = 0;
		//add all the topological changes to the hashmap
		while ((line1 = buffer2_split.readLine()) != null) {
	        String[] input = line1.split(" ");
	        int round = Integer.parseInt(input[0]);
	        lastTop = round;
	        int num1 = Integer.parseInt(input[1]);
	        int num2 = Integer.parseInt(input[2]);
	        int length = Integer.parseInt(input[3]);
	        Integer[] tuple = new Integer[] {num1, num2, length};
	        ArrayList<Integer[]> list = topChange.get(round);
	        if (list == null) {
	        	list = new ArrayList<Integer[]>();
	        }
	        list.add(tuple);
	        topChange.put(round, list);    
		}
		buffer2_split.close();
		
		System.out.println("Round 1" );
		if (flag == 1) writer2.println("Round 1" );
		for (int i = 1; i < total; i++) {
			//after adding all the edges in, initialize the table
			routers[i].initialize();
			Pair[][] pair = routers[i].getTable();
			for (int j = 0; j < total; j++) {
				if (j == 0) {
					System.out.print(i + " ");
					if (flag == 1) writer2.print(i + "\t");
				} else if (pair[i][j] == null) {
					System.out.print("-1,-1" + "\t");
					if (flag == 1) writer2.print("-1,-1" + " \t");
				} else {
					System.out.print(pair[i][j] + "\t");
					if (flag == 1) writer2.print(pair[i][j] + " \t");
				}
			}
			System.out.println();
			if (flag == 1) writer2.println();
			
		}
		iterations = 1;
		converged = false;
		while (!converged || iterations < lastTop) {
			converged = true;
			iterations++;
			ArrayList<Integer[]> event = topChange.get(iterations);	
			if (event != null) {
				//System.out.println("some edges are updated");
				for (Integer[] tuple : event) {
					routers[tuple[0]].edgeUpdate(tuple[1], tuple[2]);
					routers[tuple[1]].edgeUpdate(tuple[0], tuple[2]);
				}
			}
			System.out.println("Round: " + iterations);
			//each router get distance vectors from all it's neighbors
			for (int r = 1; r < total; r++) {
				Router receiver = routers[r];
				Set<Integer> neighbor = receiver.getNeighbors().keySet();
				//get vectors from neighbor
				for (Integer n : neighbor) {
					Pair[] vector = routers[n].getSplit(r);
					receiver.receive(n, vector);
				}	
			}	
			//perform update on each routers
			for (int r = 1; r < total; r++) {
				boolean changed = routers[r].run();  //indicated whether an update occured
				converged = converged && !changed;
			}
			for (int index = 1; index < total; index++) {
				Pair[][] pair = routers[index].getTable();
				for (int j = 0; j < total; j++) {
					if (j == 0 && keepRunning) {
						System.out.print(index + " ");
					} else if (keepRunning) {
						if (pair[index][j] == null) {
							System.out.print(pair[index][j] + "\t" );
						} else if (pair[index][j].gethopCount() > 100) {
							String msg = "The program has encountered a Count-to-infinity instability.";
							System.out.println("\n" + msg);
							writer2.println(msg);
							writer2.close();
							keepRunning = false;
							converged = true;
						} else {
							System.out.print(pair[index][j] + "\t" );
						}
					}
				}
				System.out.println("");
			}
			if (flag == 1 && keepRunning) {
				writer2.println("Round " + iterations);
				for (int index = 1; index < total; index++) {
					Pair[][] pair = routers[index].getTable();
					for (int j = 0; j < total; j++) {
						if (j == 0) {
							writer2.print(index + "\t");
						} else if (pair[index][j] == null) {
							writer2.print("-1,-1" + " \t" );
						} else {
							writer2.print(pair[index][j] + " \t" );
						}
					}
					writer2.println("");
				}
			}
		}
		if (flag == 0 && keepRunning) {
			for (int index = 1; index < total; index++) {
				Pair[][] pair = routers[index].getTable();
				for (int j = 0; j < total; j++) {
					if (j == 0) {
						writer2.print(index + "\t");
					} else if (pair[index][j] == null) {
						writer2.print("-1,-1" + " \t" );
					} else {
						writer2.print(pair[index][j] + " \t" );
					}	
				}
				writer2.println("");
			}
		}
		if (keepRunning) {
			System.out.printf("\nConvergence delay: %d round\n\n", iterations - lastTop);
			writer2.println("");
			writer2.printf("\nConvergence delay: %d round\n\n", iterations - lastTop);
			writer2.close();
		}
		
		
		//Mode: Poison reverse
		keepRunning = true;
		BufferedReader buffer_poison = new BufferedReader(new FileReader(file1));
		line = buffer_poison.readLine();
		total = Integer.parseInt(line) + 1;  //because index 0 is not used
		routers = new Router[total];
		PrintWriter writer3 = new PrintWriter("Output_poison.txt","UTF-8");
		//Construct n number of routers
		for (int i = 1; i < total; i++) {
			routers[i] = new Router(i, total);
		}
		//add all the edges between
		while ((line = buffer_poison.readLine()) != null) {
	        String[] input = line.split(" ");
	        int num1 = Integer.parseInt(input[0]);
	        int num2 = Integer.parseInt(input[1]);
	        int length = Integer.parseInt(input[2]);
	        Router r1 = routers[num1];
	        Router r2 = routers[num2];
	        r1.addNeighbors(num2, length);
	        r2.addNeighbors(num1, length);      
		}
		buffer_poison.close();
		BufferedReader buffer2_poison = new BufferedReader(new FileReader(file2));
		//integer = iteration  arraylist = updated edges
		topChange = new HashMap<>();
		line1 = "";
		lastTop = 0;
		//add all the topological changes to the hashmap
		while ((line1 = buffer2_poison.readLine()) != null) {
	        String[] input = line1.split(" ");
	        int round = Integer.parseInt(input[0]);
	        lastTop = round;
	        int num1 = Integer.parseInt(input[1]);
	        int num2 = Integer.parseInt(input[2]);
	        int length = Integer.parseInt(input[3]);
	        Integer[] tuple = new Integer[] {num1, num2, length};   
	        ArrayList<Integer[]> list = topChange.get(round);
	        if (list == null) {
	        	list = new ArrayList<Integer[]>();
	        }
	        list.add(tuple);
	        topChange.put(round, list); 
		}
		buffer2_poison.close();
		System.out.println("Round 1" );
		if (flag == 1) writer3.println("Round 1" );
		for (int i = 1; i < total; i++) {
			//after adding all the edges in, initialize the table
			routers[i].initialize();
			Pair[][] pair = routers[i].getTable();
			for (int j = 0; j < total; j++) {
				if (j == 0) {
					System.out.print(i + " ");
					if (flag == 1) writer3.print(i + "\t");
				} else if (pair[i][j] == null) {
					System.out.print("-1,-1" + "\t");
					if (flag == 1) writer3.print("-1,-1" + " \t");
				} else {
					System.out.print(pair[i][j] + "\t");
					if (flag == 1) writer3.print(pair[i][j] + " \t");
				}
			}
			System.out.println();
			if (flag == 1) writer3.println();
			
		}		
		iterations = 1;
		converged = false;
		while (!converged || iterations < lastTop) {
			converged = true;
			iterations++;
			//check topological change in this iteration
			ArrayList<Integer[]> event = topChange.get(iterations);	
			if (event != null) {
				//System.out.println("some edges are updated");
				for (Integer[] tuple : event) {
					routers[tuple[0]].edgeUpdate(tuple[1], tuple[2]);
					routers[tuple[1]].edgeUpdate(tuple[0], tuple[2]);
				}
			}
			System.out.println("Round: " + iterations);
			//each router get distance vectors from all it's neighbors
			for (int r = 1; r < total; r++) {
				Router receiver = routers[r];
				Set<Integer> neighbor = receiver.getNeighbors().keySet();
				//get vectors from neighbor
				for (Integer n : neighbor) {
					Pair[] vector = routers[n].getPoison(r);
					receiver.receive(n, vector);
				}	
			}	
			//perform update on each routers
			for (int r = 1; r < total; r++) {
				boolean changed = routers[r].runPoison();
				converged = converged && !changed;
			}
			for (int index = 1; index < total; index++) {
				Pair[][] pair = routers[index].getTable();
				for (int j = 0; j < total; j++) {
					if (j == 0 && keepRunning) {
						System.out.print(index + " ");
					} else if (keepRunning) {
						if (pair[index][j] == null) {
							System.out.print(pair[index][j] + "\t" );
						} else if (pair[index][j].gethopCount() > 100) {
							String msg = "The program has encountered a Count-to-infinity instability.";
							System.out.println("\n" + msg);
							writer3.println(msg);
							writer3.close();
							keepRunning = false;
							converged = true;
						} else {
							System.out.print(pair[index][j] + "\t" );
						}
					}
				}
				System.out.println("");
			}	
			if (flag == 1 && keepRunning) {
				writer3.println("Round " + iterations);
				for (int index = 1; index < total; index++) {
					Pair[][] pair = routers[index].getTable();
					for (int j = 0; j < total; j++) {
						if (j == 0) {
							writer3.print(index + "\t");
						} else if (pair[index][j] == null) {
							writer3.print("-1,-1" + " \t" );
						} else {
							writer3.print(pair[index][j] + " \t" );
						}
					}
					writer3.println("");
				}
			}
		}
		if (flag == 0 && keepRunning) {
			for (int index = 1; index < total; index++) {
				Pair[][] pair = routers[index].getTable();
				for (int j = 0; j < total; j++) {
					if (j == 0) {
						writer3.print(index + "\t");
					} else if (pair[index][j] == null) {
						writer3.print("-1,-1" + " \t" );
					} else {
						writer3.print(pair[index][j] + " \t" );
					}
				}
				writer3.println("");
			}
		}
		
		if (keepRunning) {
			System.out.printf("\nConvergence delay: %d round\n\n", iterations - lastTop);
			writer3.println("");
			writer3.printf("\nConvergence delay: %d round\n\n", iterations - lastTop);
			writer3.close();
		}
		//end of main
	}

}
