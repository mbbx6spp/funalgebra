package funalgebra.quasiorder

/**
  * A quasiorder (sometimes called preorder in mathematics) refers
  * to a generalization of partial orderings such as ancestry trees
  * of various kinds used in distributed systems an other
  * applications (e.g. logical/vector clocks, etc.).
  *
  * Some intuitions about quasiorders:
  * <ul>
  *   <li>all partial orderings and equivalence relations are quasiorders</li>
  *   <li>there is a correspondence between a quasiorder and a directed graph</li>
  *   <li>not all directed graphs correspond to a quasiorder though</li>
  *   <li>
  *     elements of the set correspond to vertices in the directed graph
  *     and the directed edges connecting a pair of vertices determine
  *     the quasiorder result (i.e. given vertice `a` can I navigate to `b`
  *     via directed edges in given graph or not).
  *   </li>
  *   <li>quasiorder expresses "reachability" in a directed graph</li>
  *   <li>
  *     "reachability" in directed acyclic graphs (graphs that do not
  *     contain any cycles) produces partially ordered sets, which are
  *     specializations of quasiordered sets (aka prosets) satisfying
  *     additional anti-symmetric property.
  *   </li>
  * </ul>
  *
  * <h3>Formal Definition</h3>
  *
  * A quasiorder is a binary relation, `~&lt;` for a pair of elements, `a` and `b`,
  * belonging to a set `S`, which satisfies the following conditions:
  *
  * <ul>
  *   <li>reflexivity: `a ~&lt; a`</li>
  *   <li>transitivity: `if a ~&lt; b and b ~&lt; c, then a ~&lt; c`</li>
  * </ul>
  */

trait QuasiOrder[A] {
  def reachable(a1: A, a2: A): Boolean
}

