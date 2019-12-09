

import java.util.ArrayList;
import java.util.LinkedList;

public class SparseGraph<V, E> implements Graph<V, E> {

	class SGVertex implements Vertex<V> {

		int id;
		Object label;
		V data;

		ArrayList<Edge<E>> outgoing;
		ArrayList<Edge<E>> incoming;

		SGVertex(V v) {
			id = vertexCount;
			vertexCount++;
			this.data = v;
			this.outgoing = new ArrayList<Edge<E>>();
			this.incoming = new ArrayList<Edge<E>>();
		}

		@Override
		public V get() {
			return data;
		}

		@Override
		public void put(V v) {
			data = v;
		}
	}

	class SGEdge implements Edge<E> {

		int id;
		Vertex<V> from;
		Vertex<V> to;
		Object label;
		E data;

		SGEdge(Vertex<V> from, Vertex<V> to, E e) {
			id = edgeCount;
			edgeCount++;
			this.from = from;
			this.to = to;
			data = e;
		}

		@Override
		public E get() {
			return data;
		}

		@Override
		public void put(E e) {
			data = e;
		}

	}

	static int vertexCount;
	static int edgeCount;
	ArrayList<Vertex<V>> vertices;
	ArrayList<Edge<E>> edges;
	ArrayList<Boolean> vertexPresent;
	ArrayList<Boolean> edgePresent;

	public SparseGraph() {
		vertices = new ArrayList<Vertex<V>>();
		edges = new ArrayList<Edge<E>>();
		vertexPresent = new ArrayList<Boolean>();
		edgePresent = new ArrayList<Boolean>();
		vertexCount = 0;
		edgeCount = 0;
	}

	private SGVertex convert(Vertex<V> v) throws PositionException {

		if (v == null) {
			throw new PositionException();
		}
		try {
			SGVertex n = (SGVertex) v;
			return n;
		} catch (ClassCastException e) {
			throw new PositionException();
		}
	}

	private SGEdge convert(Edge<E> edge) throws PositionException {

		if (edge == null) {
			throw new PositionException();
		}

		try {
			SGEdge n = (SGEdge) edge;
			return n;
		} catch (ClassCastException e) {
			throw new PositionException();
		}
	}

	@Override
	public Vertex<V> insert(V v) {
		SGVertex sgv = new SGVertex(v);
		vertices.add(sgv);
		vertexPresent.add(true);
		return sgv;
	}

	@Override
	public Edge<E> insert(Vertex<V> from, Vertex<V> to, E e) throws PositionException, InsertionException {

		if (from == to) {
			throw new InsertionException();
		}

		SGEdge sge = new SGEdge(from, to, e);
		SGVertex sgFrom = convert(from);
		SGVertex sgTo = convert(to);

		if (!vertexPresent.get(sgFrom.id) || !vertexPresent.get(sgTo.id)) {
			throw new PositionException();
		}

		for (Edge<E> edge : sgFrom.outgoing) {
			SGEdge sgfromedge = convert(edge);
			if (sge.from == sgfromedge.from && sge.to == sgfromedge.to) {
				throw new InsertionException();
			}
		}

		edgePresent.add(true);
		edges.add(sge);
		sgFrom.outgoing.add(sge);
		sgTo.incoming.add(sge);
		return sge;
	}

	@Override
	public V remove(Vertex<V> v) throws PositionException, RemovalException {

		SGVertex vertex = convert(v);

		if (!vertex.outgoing.isEmpty() || !vertex.incoming.isEmpty()) {
			throw new RemovalException();
		}

		if (!vertexPresent.get(vertex.id)) {
			throw new PositionException();
		}

		vertices.remove(vertex);
		vertexPresent.set(vertex.id, false);

		return vertex.data;
	}

	@Override
	public E remove(Edge<E> e) throws PositionException {

		SGEdge edge = convert(e);

		if (!edgePresent.get(edge.id)) {
			throw new PositionException();
		}

		SGVertex fromVertex = convert(edge.from);
		SGVertex toVertex = convert(edge.to);
		fromVertex.outgoing.remove(edge);
		toVertex.incoming.remove(edge);
		edges.remove(edge);
		edgePresent.set(edge.id, false);
		return edge.data;
	}

	@Override
	public Iterable<Vertex<V>> vertices() {
		return new ArrayList<Vertex<V>>(vertices);
	}

	@Override
	public Iterable<Edge<E>> edges() {
		return new ArrayList<Edge<E>>(edges);
	}

	@Override
	public Iterable<Edge<E>> outgoing(Vertex<V> v) throws PositionException {
		SGVertex vertex = convert(v);
		if (!vertexPresent.get(vertex.id)) {
			throw new PositionException();
		}
		return new ArrayList<Edge<E>>(convert(v).outgoing);
	}

	@Override
	public Iterable<Edge<E>> incoming(Vertex<V> v) throws PositionException {
		SGVertex vertex = convert(v);
		if (!vertexPresent.get(vertex.id)) {
			throw new PositionException();
		}
		return new ArrayList<Edge<E>>(convert(v).incoming);
	}

	@Override
	public Vertex<V> from(Edge<E> e) throws PositionException {
		SGEdge edge = convert(e);
		if (!edgePresent.get(edge.id)) {
			throw new PositionException();
		}
		return edge.from;
	}

	@Override
	public Vertex<V> to(Edge<E> e) throws PositionException {
		SGEdge edge = convert(e);
		if (!edgePresent.get(edge.id)) {
			throw new PositionException();
		}
		return edge.to;
	}

	@Override
	public void label(Vertex<V> v, Object l) throws PositionException {
		SGVertex vertex = convert(v);
		if (!vertexPresent.get(vertex.id)) {
			throw new PositionException();
		}
		vertex.label = l;
	}

	@Override
	public void label(Edge<E> e, Object l) throws PositionException {
		SGEdge edge = convert(e);
		if (!edgePresent.get(edge.id)) {
			throw new PositionException();
		}
		edge.label = l;
	}

	@Override
	public Object label(Vertex<V> v) throws PositionException {
		SGVertex vertex = convert(v);
		if (!vertexPresent.get(vertex.id)) {
			throw new PositionException();
		}
		return vertex.label;
	}

	@Override
	public Object label(Edge<E> e) throws PositionException {
		SGEdge edge = convert(e);
		if (!edgePresent.get(edge.id)) {
			throw new PositionException();
		}
		return edge.label;
	}

	@Override
	public void clearLabels() {
		for (Vertex<V> vertex : vertices) {
			convert(vertex).label = null;
		}

		for (Edge<E> edge : edges) {
			convert(edge).label = null;
		}
	}

	public String toString() {
		String line = "digraph {\n";

		for (Vertex<V> v : vertices) {
			SGVertex vertex = convert(v);
			line += "  \"" + vertex.data.toString() + "\";\n";
		}

		for (Edge<E> e : edges) {
			SGEdge edge = convert(e);
			SGVertex fromVertex = convert(edge.from);
			SGVertex toVertex = convert(edge.to);
			line += "  \"" + fromVertex.data.toString() + "\"";
			line += " -> ";
			line += "\"" + toVertex.data.toString() + "\" ";
			line += "[label=\"" + edge.data.toString() + "\"];\n";
		}
		line += "}";
		return line;
	}

}
