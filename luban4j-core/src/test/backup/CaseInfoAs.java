package neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.TraversalDescription;

import java.io.File;
import java.util.Map;

/**
 * Created by cgz on 2018-04-17 15:29
 * 描述：
 */
public class CaseInfoAs {

//    private static final String DB_PATH = "testgraph.db";
    private static final String DB_PATH = "target/graph.db";

    private GraphDatabaseService graphDB;

    private enum RelTypes implements RelationshipType {
        CRIME, LINK
    }


    public void init() {
        graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(DB_PATH));
    }

    /**创建数据*/
    public void create() {

        Transaction tx = graphDB.beginTx();

        Node case1 = graphDB.createNode(new CaseLabel("CASEINFO"));
        case1.setProperty("name", "案件1");
        case1.setProperty("address", "南山");

        Node case2 = graphDB.createNode(new CaseLabel("CASEINFO"));
        case2.setProperty("name", "案件2");
        case2.setProperty("address", "福田");

        Node case3 = graphDB.createNode(new CaseLabel("CASEINFO"));
        case3.setProperty("name", "案件3");
        case3.setProperty("address", "龙华");

        Node userA = graphDB.createNode(new CaseLabel("PERSON"));
        userA.setProperty("name", "A");
        userA.setProperty("idcard", "150302198012228239");
        userA.setProperty("tel", "13685246639");

        Node userB = graphDB.createNode(new CaseLabel("PERSON"));
        userB.setProperty("name", "B");
        userB.setProperty("idcard", "370634199208304929");
        userB.setProperty("tel", "13885246670");

        Node userC = graphDB.createNode(new CaseLabel("PERSON"));
        userC.setProperty("name", "C");
        userC.setProperty("idcard", "430721198802065735");
        userC.setProperty("tel", "13966704782");

        Node userD = graphDB.createNode(new CaseLabel("PERSON"));
        userD.setProperty("name", "D");
        userD.setProperty("idcard", "522730198707118747");
        userD.setProperty("tel", "13670478962");

        Node userE = graphDB.createNode(new CaseLabel("PERSON"));
        userE.setProperty("name", "E");
        userE.setProperty("idcard", "622926198609158032");
        userE.setProperty("tel", "13047829667");

        Node userF = graphDB.createNode(new CaseLabel("PERSON"));
        userF.setProperty("name", "F");
        userF.setProperty("idcard", "500114197706138305");
        userF.setProperty("tel", "13478296670");

        Node userG = graphDB.createNode(new CaseLabel("PERSON"));
        userG.setProperty("name", "G");
        userG.setProperty("idcard", "500114106138305152");
        userG.setProperty("tel", "13476670156");

        Node userH = graphDB.createNode(new CaseLabel("PERSON"));
        userH.setProperty("name", "H");
        userH.setProperty("idcard", "500114197704751236");
        userH.setProperty("tel", "13296156670");

        case1.createRelationshipTo(userA, RelTypes.CRIME);
        case1.createRelationshipTo(userB, RelTypes.CRIME);
        case1.createRelationshipTo(userC, RelTypes.CRIME);
        case1.createRelationshipTo(userD, RelTypes.CRIME);

        case2.createRelationshipTo(userB, RelTypes.CRIME);
        case2.createRelationshipTo(userC, RelTypes.CRIME);
        case2.createRelationshipTo(userE, RelTypes.CRIME);
//        case2.createRelationshipTo(userD, RelTypes.CRIME);


        case3.createRelationshipTo(userF, RelTypes.CRIME);
        case3.createRelationshipTo(userG, RelTypes.CRIME);
        case3.createRelationshipTo(userH, RelTypes.CRIME);

        tx.success();
        tx.close();

    }

    public void search(String username) {

        Transaction tx = graphDB.beginTx();

        Node startNode = graphDB.findNode(new CaseLabel("PERSON"), "name", username);

        Iterable<Relationship> iterable = startNode
                .getRelationships(RelTypes.CRIME, Direction.INCOMING);
        for (Relationship r : iterable) {

            Node node = r.getStartNode();
            long id = node.getId();
            String name = (String)node.getProperty("name");
//            String idcard = (String)node.getProperty("idcard");
//            String tel = (String)node.getProperty("tel");

            System.out.println(id + " " + name+ " ");
        }
        tx.success();
        tx.close();
    }

    /**遍历查找**/
    public void searchUser(String username) {
        Transaction tx = graphDB.beginTx();

        Node startNode = graphDB.findNode(new CaseLabel("PERSON"), "name", username);
//        Node startNode = graphDB.findNode(new CaseLabel("PERSON"), "name", "B");

        TraversalDescription td = graphDB.traversalDescription();
        td.relationships(RelTypes.CRIME, Direction.OUTGOING);//沿着关系 TONG
        td.depthFirst();//设置深度优先
//        td.evaluator(Evaluators.excludeStartPosition());

        //从开始节点开始

        Iterable<Node> it = td.traverse(startNode).nodes();
        for (Node node : it) {
            long id = node.getId();
            if( node.hasLabel( new CaseLabel("PERSON") )) {
                String name = (String)node.getProperty("name");
                String idcard = (String)node.getProperty("idcard");
                String tel = (String)node.getProperty("tel");
                System.out.println(id + " " + name+ " " + idcard + " " + tel);
            } else {
//                String name = (String)node.getProperty("name");
//                String address = (String)node.getProperty("address");
//                System.out.println(id + " " + name+ " " + address);
            }
        }

        tx.success();
        tx.close();
    }
    public void searchByCypher(){
        String q="match (n:PERSON) RETURN n";
        Result result = graphDB.execute(q);
        while (result.hasNext()) {
            Map<String, Object> map = result.next();
            System.out.println(map);
        }

    }
    /**查询所有案件信息*/
    public void searchAllCase() {

        Transaction tx = graphDB.beginTx();

        ResourceIterator<Node> iterator = graphDB.findNodes(new CaseLabel("CASEINFO"));
        for (ResourceIterator<Node> it = iterator; it.hasNext(); ) {
            Node node = it.next();
            long id = node.getId();
            String name = (String)node.getProperty("name");
            String address = (String)node.getProperty("address");
            System.out.println(id + " " + name+ " " + address);
        }

        tx.success();
        tx.close();
    }

    /**查询所有作案人员信息*/
    public void searchAllPerson() {

        Transaction tx = graphDB.beginTx();

        ResourceIterator<Node> iterator = graphDB.findNodes(new CaseLabel("PERSON"));
        for (ResourceIterator<Node> it = iterator; it.hasNext(); ) {
            Node node = it.next();
            long id = node.getId();
            String name = (String)node.getProperty("name");
            String idcard = (String)node.getProperty("idcard");
            String tel = (String)node.getProperty("tel");

            System.out.println(id + " " + name+ " " + idcard + " " + tel);
        }

        tx.success();
        tx.close();
    }

    public static void main(String[] args) {
        long l = System.currentTimeMillis();
        CaseInfoAs as = new CaseInfoAs();
        as.init();
        as.create();
//        as.search("A");
//        as.search1();
//        as.searchAllCase();
//        as.searchUser("A");
        as.searchByCypher();
        System.out.println(System.currentTimeMillis() - l);
    }
}

class CaseLabel implements Label {
    private String name;
    public CaseLabel(String name) {
        this.name = name;
    }

    public String name(){
        return name;
    }
}