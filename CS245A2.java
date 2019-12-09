
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Scanner;
import java.util.LinkedList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 * Six Degrees of actor x.
 * 
 */
public final class CS245A2 {
	// Graph holding movies and actors as vertices, relationships
	// as edges. All are simply strings.
	private static Graph<String, String> graph = new SparseGraph<String, String>();
	static Map<String, Vertex<String>> vertices = new HashMap<>();

	// Vertices for the actor we're trying to connect to target and for the
	// target actor.
	private static Vertex<String> actor;
	private static Vertex<String> target;

	// Shut up checkstyle.
	private CS245A2() {
	}

	// Read input file and turn it into a Graph.
	//
	// Generates a bipartite graph in which BOTH movies and actors
	// are vertices. A graph in which all vertices are actors and
	// movies are edges would be bad
	//
	// This function also sets up the "actor" and "target" globals
	// that will be used to direct the search.
	// crew is not of concern because
	// it should be the co-stardom network
	private static void readInput(String filename) throws FileNotFoundException, IOException {
		HashSet<String> createdEdges = new HashSet<>();

		// keep track of all vertices created so far by name

		// how we read the input
		BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
		String line;

		line = reader.readLine(); // remove headers

		while ((line = reader.readLine()) != null) {

			// Because there are commas in the middle of the cast and crew
			// fields, these fields are identified by the square brackets
			int ind = line.lastIndexOf('[');
			line = line.substring(0, ind); // crew is not needed since it is
											// co-stardom network

			// Need to deal with movies that have brackets in the name like
			// [REC]²
			// Need to deal with a character that is identified as [Singing
			// voice]
			// Need to deal with movies with an empty cast listing []
			String cast = null;
			String data = null;
			if (line.indexOf("[{") != -1) {
				cast = line.substring(line.indexOf("[{"), line.lastIndexOf(']') + 1);
				data = line.substring(0, line.indexOf("[{"));
			} else if (line.indexOf("[]") != -1) {
				cast = "[]";
				data = line.substring(0, line.indexOf("["));
			}
			String[] tokens = data.split(",");

			// find or create vertex for the movie
			Vertex<String> m = vertices.get(tokens[1]);
			if (m == null) {
				m = graph.insert(tokens[1]);
				vertices.put(tokens[1], m);
			}

			cast = cast.replaceAll("\"\"", "\""); // double quotes are read in
													// from the file, replace
													// two quotes with one

			JSONArray arr = (JSONArray) JSONValue.parse(cast); // array of cast
																// members

			for (int i = 0; i < arr.size(); i++) {
				JSONObject map = (JSONObject) arr.get(i); // key-value map
				String name = (String) map.get("name");
				name = name.toLowerCase(); // make it case-insensitive

				// find or create vertex for the actor
				Vertex<String> a = vertices.get(name);
				if (a == null) {
					a = graph.insert(name);
					vertices.put(name, a);
				}

				// create two edges, from and to the movie
				// One issue is that sometimes in the data, a single movie
				// will have the same member listed twice
				// for instance: keith richards is listed as a cast member twice
				// for Pirates of the Carribean (At World's End)

				String features = m.get() + " features " + a.get();
				String actsIn = a.get() + " acts in " + m.get();

				if (!createdEdges.contains(features)) {
					graph.insert(m, a, "features");
					createdEdges.add(features);
				}

				if (!createdEdges.contains(actsIn)) {
					graph.insert(a, m, "acts in");
					createdEdges.add(actsIn);
				}

			}
		}

		reader.close();
	}

	private static void solveSixDegrees() {
		Queue<Vertex<String>> queue = new LinkedList<Vertex<String>>();

		HashMap<Vertex<String>, Edge<String>> predecessor = new HashMap<Vertex<String>, Edge<String>>();
		HashMap<Vertex<String>, String> color = new HashMap<Vertex<String>, String>();

		for (Vertex<String> v : graph.vertices()) {
			predecessor.put(v, null);
			color.put(v, "white");
		}

		predecessor.put(target, null);
		color.put(target, "gray");

		queue.add(target);

		HashSet<Vertex<String>> visited = new HashSet<>();
		Vertex<String> candidate;

		while (!queue.isEmpty()) {
			candidate = queue.remove();
			visited.add(candidate);
			if (candidate == actor) {
				int i = 0;
				do {
					if (i % 2 == 0) {
						// even vertices are actors, odd are movies
						System.out.print(candidate.get() + " --> ");
					}
					Edge<String> e = predecessor.get(candidate);
					candidate = graph.from(e);

					i++;

				} while (predecessor.get(candidate) != null);
				System.out.println(candidate.get());
				break;
			}
			for (Edge<String> e : graph.outgoing(candidate)) {
				Vertex<String> dest = graph.to(e);
				if (color.get(dest).equals("white")) {
					color.put(dest, "gray");
					predecessor.put(dest, e);
					if (!visited.contains(dest))
						queue.add(dest);
				}
			}
			color.put(candidate, "black");
		}
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws FileNotFoundException
	 *             If database file cannot be opened.
	 * @throws IOException
	 *             If database file cannot be read properly.
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {

		// prompt user for both actors here

		// read the input
		readInput(args[0]);

		// make case-insensitive
		Scanner sc = new Scanner(System.in);
		System.out.print("Actor 1 name: ");
		String actorString = sc.nextLine().toLowerCase();
		if (!vertices.containsKey(actorString)) {
			System.out.printf("No such actor \n", args[1]);
			System.exit(1);
		}
		System.out.print("Actor 2 name: ");
		String targetString = sc.nextLine().toLowerCase();
		if (!vertices.containsKey(targetString)) {
			System.out.printf("No such actor \n", args[1]);
			System.exit(1);
		}

		actor = vertices.get(actorString);
		target = vertices.get(targetString);

		// play "six degrees of actor target" using breadth-first search
		solveSixDegrees();

		sc.close();
	}
}
