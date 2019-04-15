package com.company;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            new GrammaticalAnalysis();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test(){
        Production p=new Production("T",Arrays.asList("T","*","F"));
        Production p1=new Production("T",Arrays.asList("T","*","F"));
        Item item=new Item(p,0);
        Item item1=new Item(p1,0);
        HashSet<Item> set=new HashSet<>();
        set.add(item);
        Assert.assertFalse(set.contains(item1));
        Assert.assertEquals(item, item1);
        Assert.assertEquals(item, item);
        Assert.assertEquals(p, p1);
    }
}

/**
 * 产生式,代表文法当中一个个产生式
 */
class GrammaticalAnalysis{
    private List<Production> productions;//产生式
    private Map<String,HashSet<String>> FIRST;//FIRST符号集
    private Map<String,HashSet<String>> FOLLOW;//FOLLOW符号集
    private Set<String> table;//符号表
    private Set<String> nonTerminals;//非终结符
    private Map<String,Integer> indexes=new HashMap<>();
    private List<Cluster> clusters;//聚簇集合,可以通过索引找到对应的cluster
    private Map<Cluster,Integer> clusterIntMap;//通过cluster找到对应的索引


    private List<String> cols;
    private String[][] analyticalTable;//分析表
    public GrammaticalAnalysis() throws FileNotFoundException {
        productions=new ArrayList<>();
        FIRST=new HashMap<>();
        FOLLOW=new HashMap<>();
        table=new HashSet<>();
        clusters=new ArrayList<>();
        nonTerminals =new HashSet<>();
        clusterIntMap=new HashMap<>();
        InitProductions();
        for (String s : nonTerminals) {
            findFirsts(s);//获得了first集
        }
        for (String s: nonTerminals){
            findFollows(s);
        }
        makeCLusters();//得到所有的cluster列表
        makeAnalyticalTable();//获得对应的表
        PrintStream stream=new PrintStream(new File("/home/xiao/IdeaProjects/编译原理实验2_1/src/语法分析表"));
        stream.print("      |");
        for (String s : cols) {
            stream.printf("%-6s|",s);
        }
        stream.println();
        for (int i = 0; i < analyticalTable.length; i++) {
            stream.printf("%-6d|",i);
            String[] strings = analyticalTable[i];
            for (String string : strings) {
                if(string!=null) {
                    stream.printf("%-6s|", string);
                }else {
                    stream.print("      |");
                }
            }
            stream.println();
        }

    }
    private void InitProductions(){
        table.addAll(Arrays.asList(";","int","double","float","id","=","+","-","*","/","(",")","num","if","then","else","do","while"));
        nonTerminals.addAll(Arrays.asList("P1","P","D1","S1","D","S1","S","T","L","E","F"));
        productions.add(new Production("P1", Collections.singletonList("P")));//不能忘记开始符号
        productions.add(new Production("P", Arrays.asList("D1","S1")));
        productions.add(new Production("D1",Arrays.asList("D",";","D1")));
        productions.add(new Production("D1", new ArrayList<>()));
        productions.add(new Production("S1",Arrays.asList("S",";","S1")));
        productions.add(new Production("S1", new ArrayList<>()));
        productions.add(new Production("D",Arrays.asList("T","L")));
        productions.add(new Production("T", Collections.singletonList("int")));
        productions.add(new Production("T", Collections.singletonList("float")));
        productions.add(new Production("T", Collections.singletonList("double")));
        productions.add(new Production("L", Collections.singletonList("id")));
        productions.add(new Production("S",Arrays.asList("id","=","E")));
        productions.add(new Production("E", Collections.singletonList("T")));
        productions.add(new Production("E",Arrays.asList("E","+","T")));
        productions.add(new Production("E",Arrays.asList("E","-","T")));
        productions.add(new Production("T", Collections.singletonList("F")));
        productions.add(new Production("T",Arrays.asList("T","*","F")));
        productions.add(new Production("T",Arrays.asList("T","/","F")));
        productions.add(new Production("F",Arrays.asList("(","E",")")));
        productions.add(new Production("F", Collections.singletonList("id")));
        productions.add(new Production("F", Collections.singletonList("num")));
        productions.add(new Production("S",Arrays.asList("if","E","then","S1","else","S1")));
        productions.add(new Production("S",Arrays.asList("do","S1","while","E")));
        cols=Arrays.asList(";","int","double","float","id","=","+","-","*","/","(",")","num","if","then","else","do","while","#",
                "P1","P","D1","S1","D","S","T","L","E","F");
        indexes.put(";",0);
        indexes.put("int",1);
        indexes.put("double",2);
        indexes.put("float",3);
        indexes.put("id",4);
        indexes.put("=",5);
        indexes.put("+",6);
        indexes.put("-",7);
        indexes.put("*",8);
        indexes.put("/",9);
        indexes.put("(",10);
        indexes.put(")",11);
        indexes.put("num",12);
        indexes.put("if",13);
        indexes.put("then",14);
        indexes.put("else",15);
        indexes.put("do",16);
        indexes.put("while",17);
        indexes.put("#",18);
        indexes.put("P1",19);
        indexes.put("P",20);
        indexes.put("D1",21);
        indexes.put("S1",22);
        indexes.put("D",23);
        indexes.put("S",24);
        indexes.put("T",25);
        indexes.put("L",26);
        indexes.put("E",27);
        indexes.put("F",28);
    }
    private void findFirsts(String s){
        if(FIRST.containsKey(s)){
            return;
        }
        FIRST.put(s,new HashSet<>());
        for (Production production : productions) {
            if(production.left.equals(s)){
                for (int i = 0; i < production.rights.size(); i++) {
                    String f=production.rights.get(i);
                    if(f.equals(s)){
                        continue;//检查下一项
                    }
                    if(table.contains(f)){//是个终结符
                    FIRST.get(s).add(f);
                    break;
                    }else {//是个非终结符
                        if (!FIRST.containsKey(f)) {
                            findFirsts(f);
                        }
                        FIRST.get(s).addAll(FIRST.get(f));
                        if (!productions.contains(new Production(f, new ArrayList<>()))) {//不能够推到出空产生式
                            break;
                        }
                    }
                }
            }
        }
    }
    private void makeAnalyticalTable(){
        int m=clusters.size();//获得状态的个数
        int n=table.size()+1+ nonTerminals.size();
//        table.addAll(Arrays.asList(";","int","double","float","id","=","+","-","*","/","(",")","num","if","then","else","do","while"));
        analyticalTable=new String[m][n];//获得了分析表
        for (Cluster cluster : clusters) {
            int id = cluster.clusterID;//一定要对应其ID
            for (String s : cluster.nexts.keySet()) {
                Cluster cluster1 = cluster.nexts.get(s);
                if (!nonTerminals.contains(s)) {//是个非终结符,包括#
                    int j = clusterIntMap.get(cluster1);//跳到第j个状态
                    analyticalTable[id][indexes.get(s)] = "S" + j;//跳转到下一个状态
                } else {
                    int j = clusterIntMap.get(cluster1);
                    analyticalTable[id][indexes.get(s)] = String.valueOf(j);
                }
            }
            for (Item item : cluster.items) {//进行规约项目的处理
                if (item.len == item.loc) {//进行规约处理
                    Set<String> follow = FOLLOW.get(item.production.left);//找到对应的follow集合
                    for (String s : follow) {
                        analyticalTable[id][indexes.get(s)] = "r" + productions.indexOf(item.production);
                    }
                }
            }
        }
    }
    /**
     * 找到一个符号的
     * @param s 一个字符
     */
    private void findFollows(String s){
        if(FOLLOW.containsKey(s)){
            return;
        }
        FOLLOW.put(s,new HashSet<>());
        if(s.equals("P1")){
            FOLLOW.get(s).add("#");
        }
        for (Production production : productions) {
            if(production.rights.contains(s)){//某个产生式右部存在S
                int index=0;
                for (int i = 0; i < production.rights.size(); i++) {
                    if(production.rights.get(i).equals(s)){
                        index=i;
                        break;
                    }
                }
                if(index==production.rights.size()-1){//已经到达了最后
                    findFollows(production.left);
                    FOLLOW.get(s).addAll(FOLLOW.get(production.left));//把left的follow集中元素全部加进来
                }else {
                    int x1=index+1;
                    while (x1<=production.rights.size()) {//到达末尾也自动退出
                        if(x1==production.rights.size()){
                            findFollows(production.left);
                            FOLLOW.get(s).addAll(FOLLOW.get(production.left));//把left的follow集中元素全部加进来
                            break;
                        }
                        String s1=production.rights.get(x1);
                        if (table.contains(s1)) {//下一个是非终结符
                            FOLLOW.get(s).add(s1);
                            break;
                        } else {
                            FOLLOW.get(s).addAll(FIRST.get(s1));//把下一个字符的首符号集加进去
                            if(productions.contains(new Production(s1,new ArrayList<>()))){//能够推导到终结符,目前这里还只是一步推导之后扩展到多步推导
                                x1++;
                            }else {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void makeCLusters(){
        Set<Cluster> clusterSet;//簇组成的集合
        Queue<Cluster> clusterQueue;//簇组成的队列
        clusterSet=new HashSet<>();
        clusterQueue=new LinkedList<>();
        Production firstPro=productions.get(0);
        int id=0;
        Item item=new Item(firstPro,0);//获取第一个项目
        Cluster firstCluster=getClusterReferItems(item);
        clusterQueue.offer(firstCluster);
        //clusterSet.add(firstCluster);
        while (!clusterQueue.isEmpty()){
            Cluster cluster=clusterQueue.poll();
            clusterSet.add(cluster);//把这个加入到序列中
            cluster.clusterID=id;//这里相当于是一个双向索引
            clusterIntMap.put(cluster,id);
            id++;
            for (Item item1 : cluster.items) {
                int loc=item1.loc;
                int len=item1.len;
                if(loc+1<=len){
                    Item item2=new Item(item1.production,item1.loc+1);//得到了下一个项目
                    boolean flag=true;
                    Cluster cluster2=null;
                    for (Cluster cluster1 : clusterSet) {
                       // if (cluster1.contains(item2)){
                        if(contains1(cluster1.items,item2)){
                            flag=false;
                            cluster2=cluster1;
                        }
                    }
                    if(flag){//需要新建一个簇
                        Cluster cluster1=getClusterReferItems(item2);
                        clusterQueue.offer(cluster1);//加入到队列当中
                    //    clusterSet.add(cluster1);//加入到集合当中
                        cluster.nexts.put(item1.production.rights.get(loc),cluster1);
                    }else {
                        cluster.nexts.put(item1.production.rights.get(loc),cluster2);
                    }
                }
            }
        }
        clusters.addAll(clusterSet);
    }
    /**
     * 针对每一个Item制造出cluster
     * @param item 对应的项目
     */
    private Cluster getClusterReferItems(Item item){
        Cluster cluster=new Cluster();
        Set<Item> set=cluster.items;//获得对应的集合
        Queue<Item> queue=new LinkedList<>();
        queue.offer(item);
        while (!queue.isEmpty()){
            Item tempItem=queue.poll();
            set.add(tempItem);
            if(tempItem.production.rights.isEmpty()){//这是一个空产生式
                set.add(new Item(tempItem.production,0));//如果是空产生式的话直接把产生式加进去就可以了
            }else {
                if (tempItem.loc < tempItem.len) {
                    String left = tempItem.production.rights.get(tempItem.loc);
                    for (Production p : productions) {
                        if (p.left.equals(left)) {
                            Item item2 = new Item(p, 0);
                            if (!contains1(set,item2)) {
                                queue.offer(item2);
                              //  set.add(item2);//把该产生式加入进去
                            }
                        }
                    }
                }
            }
        }
        return cluster;//如果是规约项目的话会少一个返回
    }

    private boolean contains1(Set<Item> set,Item item){
        for (Item item1 : set) {
            if(item.equals(item1)){
                return true;
            }
        }
        return false;
    }

    /**
     * 真正的从关系表中获得产生式序列的过程.
     * @return
     */
    private List<Production> parse(){
        List<Production> productionList=new ArrayList<>();//保存最后的结果
        Stack<Integer> stateStack=new Stack<>();//状态栈
        Stack<String> tokenStack=new Stack<>();//符号栈
        stateStack.push(0);
        tokenStack.push("#");
        String pre=null;
        try {
            Scanner in=new Scanner(new File(""));
            List<String> list=new ArrayList<>();
            while (in.hasNext()){
                list.add(in.next());
            }
                int j=0;
            while (j<list.size()) {
                String string=list.get(j);
                String ch = dealToken(string);
                if (analyticalTable[stateStack.peek()][indexes.get(ch)].charAt(0) == 'S') {//这是一个移进项目
                    int state = analyticalTable[stateStack.peek()][indexes.get(ch)].charAt(1) - '0';
                    stateStack.push(state);
                    tokenStack.push(ch);
                    j++;
                } else if (analyticalTable[stateStack.peek()][indexes.get(ch)].charAt(0) == 'r') {//规约项目
                    int num = analyticalTable[stateStack.peek()][indexes.get(ch)].charAt(1) - '0';//产生式的序号
                    Production production = productions.get(num);
                    int len = production.rights.size();
                    for (int i = 0; i < len; i++) {
                        stateStack.pop();
                        tokenStack.pop();
                    }
                    String left=production.left;
                    productionList.add(production);//将这个产生式加入进去
                    tokenStack.push(left);
                    stateStack.push(Integer.valueOf(analyticalTable[stateStack.peek()][indexes.get(left)]));//将栈压入
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return productionList;
    }
    private String dealToken(String s){
        String[] strings=s.split("\t");
        if(strings[1].charAt(1)=='标'){
            return "id";//这是一个标识符
        }else if(strings[1].charAt(1)=='常'){
            return "num";
        }else {
            return strings[0];
        }
    }


}
class Production{
    String left;
    List<String> rights;

    public Production(String left, List<String> rights) {
        this.left = left;
        this.rights = rights;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Production)){
            return false;
        }
        Production p=(Production)o;
        boolean res=left.equals(p.left);
        if(rights.size()!=((Production) o).rights.size()){
            return false;
        }
        for (int i = 0; i < rights.size(); i++) {
            res&=rights.get(i).equals(((Production) o).rights.get(i));
        }
        return res;
    }
}

/**
 * 代表项目,项目是产生式加上点
 */
class Item{
    Production production;//代表对应的产生式
    int len;//产生式右部的长度
    int loc;//点的位置,从0-len
    Set<String> stringSet;
    public Item(Production production, int loc) {
        this.production = production;
        len= production.rights.size();
        this.loc=loc;
        stringSet=new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Item)){
            return false;
        }
        return this.production.equals(((Item) o).production)&&this.loc==((Item) o).loc;
    }
}

//一个聚簇,包含很多个项目
class Cluster{
    Set<Item> items;
    int clusterID;
    Map<String,Cluster> nexts;
    public Cluster(Set<Item> items) {
        this.items = items;
        nexts=new HashMap<>();
    }
    public Cluster() {
        items=new HashSet<>();
        nexts=new HashMap<>();
    }
    public void add(Item item){
        items.add(item);
    }
    boolean contains(Item item){
        return items.contains(item);
    }
}
