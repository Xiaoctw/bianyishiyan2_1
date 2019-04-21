package com.company;

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
}
class GrammaticalAnalysis{
    private List<Production> productions;//产生式
    private Map<String, HashSet<String>> FIRST;//FIRST符号集
    private Map<String,HashSet<String>> FOLLOW;//FOLLOW符号集
    private Set<String> table;//符号表
    private Set<String> nonTerminals;//非终结符
    private Map<String,Integer> indexes=new HashMap<>();
    private List<Cluster> clusters;//聚簇集合,可以通过索引找到对应的cluster
    private Map<Cluster,Integer> clusterIDMap;//通过cluster找到对应的索引
    private List<String> cols;
    private String[][] analyticalTable;//分析表
    GrammaticalAnalysis() throws FileNotFoundException {
        productions=new ArrayList<>();
        FIRST=new HashMap<>();
        FOLLOW=new HashMap<>();
        table=new HashSet<>();
        clusters=new ArrayList<>();
        nonTerminals =new HashSet<>();
        clusterIDMap =new HashMap<>();
        InitProductions();
        for (String s : nonTerminals) {
            findFirsts(s);//获得了first集
        }
        for (String s: nonTerminals){
            findFollows(s);
        }
        makeClusters();//得到所有的cluster列表
        makeAnalyticalTable();//获得对应的表
        PrintStream stream=new PrintStream(new File("/home/xiao/IdeaProjects/编译原理实验2_1/src/语法分析表"));
        stream.print("      |");
        //接下来是打印表的过程
        for (String s : cols) {
            stream.printf("%-6s|",s);
        }
        //parse("")
        List<Production> productionList=parse("/home/xiao/IdeaProjects/编译原理实验2_1/src/com/company/input");
        maketable();
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
        stream.close();
        PrintStream stream1=new PrintStream(new File("/home/xiao/IdeaProjects/编译原理实验2_1/src/产生式序列"));
        for (Production production : productionList) {
            stream1.print(production.left+"->");
            for (String right : production.rights) {
                stream1.print(right+" ");
            }
            stream1.println();
        }
        stream1.close();
        printFirstAndFollow();
        dealWithError();
    }
    private void printFirstAndFollow(){
        try {
            PrintStream stream=new PrintStream(new File("/home/xiao/IdeaProjects/编译原理实验2_1/src/FIRST"));
            for (String s : FIRST.keySet()) {
                stream.print(s+":");
                for (String s1 : FIRST.get(s)) {
                    stream.print(" "+s1);
                }
                stream.println();
            }
            PrintStream stream1=new PrintStream(new File("/home/xiao/IdeaProjects/编译原理实验2_1/src/FOLLOW"));
            for (String s : FOLLOW.keySet()) {
                stream1.print(s+":");
                for (String s1 : FOLLOW.get(s)) {
                    stream1.print(" "+s1);
                }
                stream1.println();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void InitProductions(){
        table.addAll(Arrays.asList(";","int","double","float","id","=","+","-","*","/","(",")","num","if","then","else","do","while"));
        nonTerminals.addAll(Arrays.asList("P1","P","D1","S1","D","S1","S","T","L","E","F"));
        productions.add(new Production("P1", Collections.singletonList("P")));//不能忘记开始符号
        productions.add(new Production("P", Arrays.asList("D1","S1")));
        productions.add(new Production("D1",Arrays.asList("D",";","D1")));
        productions.add(new Production("D1", Arrays.asList("D",";")));
        productions.add(new Production("S1",Arrays.asList("S",";","S1")));
        productions.add(new Production("S1", Arrays.asList("S",";")));
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
        cols=Arrays.asList(";","int","double","float","id","=","+","-","*","/","(",")","num","if","then","else","do","while","#","P1","P","D1","S1","D","S","T","L","E","F");
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
                // boolean flag=false;
                for (int i = 0; i < production.rights.size(); i++) {
                    String f=production.rights.get(i);
                    if(f.equals(s)){
                        //       flag=true;
                        //     continue;//检查下一个产生式
                        break;
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
                    int j = clusterIDMap.get(cluster1);//跳到第j个状态
                    analyticalTable[id][indexes.get(s)] = "S" + j;//跳转到下一个状态
                } else {
                    int j = clusterIDMap.get(cluster1);
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
        Item item=new Item(productions.get(0),1);
        for (Cluster cluster : clusters) {
            if(contains1(cluster.items,item)){
                analyticalTable[cluster.clusterID][indexes.get("#")]="acc";
                break;
            }
        }
    }
    private void maketable(){//e1缺少运算符,e2缺少运算数
        for (Cluster cluster : clusters) {
            if (contains1(cluster.items,new Item(new Production("E",Arrays.asList("E","-","T")),1))){
                for (int j = 0; j < cols.size(); j++) {
                    if(analyticalTable[cluster.clusterID][j]==null){
                        analyticalTable[cluster.clusterID][j]="e1";
                    }
                }
            }
            if (contains1(cluster.items,new Item(new Production("E",Arrays.asList("E","+","T")),1))){
                for (int j = 0; j < cols.size(); j++) {
                    if(analyticalTable[cluster.clusterID][j]==null){
                        analyticalTable[cluster.clusterID][j]="e1";
                    }
                }
            }
            if (contains1(cluster.items,new Item(new Production("E",Arrays.asList("E","/","T")),1))){
                for (int j = 0; j < cols.size(); j++) {
                    if(analyticalTable[cluster.clusterID][j]==null){
                        analyticalTable[cluster.clusterID][j]="e1";
                    }
                }
            }
            if (contains1(cluster.items,new Item(new Production("E",Arrays.asList("E","*","T")),1))){
                for (int j = 0; j < cols.size(); j++) {
                    if(analyticalTable[cluster.clusterID][j]==null){
                        analyticalTable[cluster.clusterID][j]="e1";
                    }
                }
            }
        }
        for (Cluster cluster : clusters) {
            if (contains1(cluster.items,new Item(new Production("E",Arrays.asList("E","-","T")),2))){
                for (int j = 0; j < cols.size(); j++) {
                    if(analyticalTable[cluster.clusterID][j]==null){
                        analyticalTable[cluster.clusterID][j]="e2";
                    }
                }
            }
            if (contains1(cluster.items,new Item(new Production("E",Arrays.asList("E","+","T")),2))){
                for (int j = 0; j < cols.size(); j++) {
                    if(analyticalTable[cluster.clusterID][j]==null){
                        analyticalTable[cluster.clusterID][j]="e2";
                    }
                }
            }
            if (contains1(cluster.items,new Item(new Production("E",Arrays.asList("E","/","T")),2))){
                for (int j = 0; j < cols.size(); j++) {
                    if(analyticalTable[cluster.clusterID][j]==null){
                        analyticalTable[cluster.clusterID][j]="e2";
                    }
                }
            }
            if (contains1(cluster.items,new Item(new Production("E",Arrays.asList("E","*","T")),2))){
                for (int j = 0; j < cols.size(); j++) {
                    if(analyticalTable[cluster.clusterID][j]==null){
                        analyticalTable[cluster.clusterID][j]="e2";
                    }
                }
            }
        }
        for (int i = 0; i < clusters.size(); i++) {
            analyticalTable[i][indexes.get("P1")]=null;
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

    private void makeClusters(){
        Set<Cluster> clusterSet;//簇组成的集合
        Queue<Cluster> clusterQueue;//簇组成的队列
        clusterSet=new HashSet<>();
        clusterQueue=new LinkedList<>();
        Production firstPro=productions.get(0);
        int id=0;
        Item item=new Item(firstPro,0);//获取第一个项目
        Cluster firstCluster=getClusterReferItems(item);
        clusterQueue.offer(firstCluster);
        clusterSet.add(firstCluster);
        while (!clusterQueue.isEmpty()){
            Cluster temCluster=clusterQueue.poll();
            temCluster.clusterID=id;//这里相当于是一个双向索引
            clusterIDMap.put(temCluster,id);
            id++;
            for (Item temItem : temCluster.items) {
                int loc=temItem.loc;
                int len=temItem.len;
                if(loc+1<=len){
                    Item newItem=new Item(temItem.production,temItem.loc+1);//得到了下一个项目
                    boolean flag=true;
                    Cluster cluster2=null;
                    for (Cluster cluster1 : clusterSet) {
                        if(contains1(cluster1.items,newItem)){//全部都包含
                            flag=false;
                            cluster2=cluster1;
                        }
                    }
                    if(flag){//需要新建一个簇
                        Cluster cluster1=getClusterReferItems(newItem);
                        clusterQueue.offer(cluster1);//加入到队列当中
                        clusterSet.add(cluster1);
                        temCluster.nexts.put(temItem.production.rights.get(loc),cluster1);
                        //  }
                    }else {
                        temCluster.nexts.put(temItem.production.rights.get(loc),cluster2);
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
        set.add(item);
        while (!queue.isEmpty()){
            Item tempItem=queue.poll();
            //   set.add(tempItem);
            if(tempItem.production.rights.isEmpty()){//这是一个空产生式
                set.add(new Item(tempItem.production,0));//如果是空产生式的话直接把产生式加进去就可以了
            }else {
                if (tempItem.loc < tempItem.len) {
                    String left = tempItem.production.rights.get(tempItem.loc);
                    for (Production p : productions) {
                        if (p.left.equals(left)) {
                            Item item2 = new Item(p, 0);
                            if (!contains1(set,item2)) {//要继承哈希函数!!!!!
                                queue.offer(item2);
                                set.add(item2);//把该产生式加入进去
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
    private List<Production> parse(String filePath){
        List<Production> productionList=new ArrayList<>();//保存最后的结果
        Stack<Integer> stateStack=new Stack<>();//状态栈
        Stack<String> tokenStack=new Stack<>();//符号栈
        stateStack.push(0);
        tokenStack.push("#");
        try {
            Scanner in=new Scanner(new File(filePath));
            List<String> list=new ArrayList<>();
            while (in.hasNext()){
                list.add(in.nextLine());
            }
            int listSize=list.size();
            int j=0;
            while (true) {
                if(j>=listSize){
                    break;
                }
                String string=list.get(j);
                String ch = dealToken(string);
                int index_i,index_j;
                if (analyticalTable[stateStack.peek()][indexes.get(ch)]==null) {
                    index_j=indexes.get(ch);
                    List<Integer> list1=new ArrayList<>();
                    for (int i1 = 0; i1 < analyticalTable.length; i1++) {
                        if(analyticalTable[i1][index_j]!=null){
                            list1.add(i1);
                        }
                    }
                    index_i= list1.get(((int)(Math.random()*list1.size())));
                    j++;
                   // return productionList;
                }else if(analyticalTable[stateStack.peek()][indexes.get(ch)].equals("acc")){
                    break;
                }else{
                    index_i=stateStack.peek();
                    index_j=indexes.get(ch);
                }
                if (analyticalTable[index_i][index_j].charAt(0) == 'S') {//这是一个移进项目
                    int state = Integer.parseInt(analyticalTable[index_i][index_j].substring(1));
                    stateStack.push(state);
                    tokenStack.push(ch);
                    j++;
                } else if (analyticalTable[index_i][index_j].charAt(0) == 'r') {//规约项目
                    int num = Integer.parseInt(analyticalTable[index_i][index_j].substring(1));//产生式的序号,注意产生式可能有很多位
                    Production production = productions.get(num);
                    int len = production.rights.size();
                    for (int i = 0; i < len; i++) {
                        if(stateStack.empty()||tokenStack.empty()){
                            break;
                        }
                        stateStack.pop();
                        tokenStack.pop();
                    }
                    String left=production.left;
                    productionList.add(production);//将这个产生式加入进去
                    tokenStack.push(left);
                    if(stateStack.empty()){
                        stateStack.add(0);
                    }
                    if(analyticalTable[stateStack.peek()][indexes.get(left)]==null){
                        stateStack.push((int) (Math.random()*clusters.size()));
                    }else {
                        stateStack.push(Integer.valueOf((analyticalTable[stateStack.peek()][indexes.get(left)])));//将栈压入
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int len=productionList.size();
        Production lastPro=productionList.get(len-1);
        if(!lastPro.equals(new Production("P1", Collections.singletonList("P")))){
            Production temp=lastPro;
            while (!temp.equals(new Production("P1", Collections.singletonList("P")))){
                String left=temp.left;
                List<Production> list = new ArrayList<>();
                for (Production production : productions) {
                    if (production.rights.contains(left)) {
                        list.add(production);
                    }
                }
                temp = list.get((int) (Math.random() * list.size()));
                productionList.add(temp);
            }
        }
        return productionList;
    }

    /**
     * 对token字进行相应的处理
     * @param s
     * @return
     */
    private String dealToken(String s){
        String[] strings= s.split("\t");
        if(strings[1].charAt(1)=='标'){
            return "id";//这是一个标识符
        }else if(strings[1].charAt(1)=='常'){
            return "num";
        }else {
            return strings[0];
        }
    }

    private void dealWithError(){
        boolean flag1,flag2;
        flag1=true;
        flag2=true;
        String filePath="/home/xiao/IdeaProjects/编译原理实验2_1/src/com/company/input";
        Scanner in;
        try {
            in=new Scanner(new File(filePath));
            List<String> tokens=new ArrayList<>();
            while (in.hasNext()){
                tokens.add(dealToken(in.nextLine()));
            }
            int i=0;
            Set<String> operators=new HashSet<>();
            operators.addAll(Arrays.asList("+","-","*","/"));
            Set<String> ope=new HashSet<>();
            ope.addAll(Arrays.asList("id","num"));
            while (i<tokens.size()){
                if(flag1) {
                    if (i > 1 && (ope.contains(tokens.get(i - 1))) && (tokens.get(i).equals("id") || tokens.get(i).equals("num"))) {
                        System.out.println("第" + i + "个token字出现错误,缺少运算符");
                        flag1 = false;
                    }
                }
                if(flag2) {
                    if (i > 1 && (operators.contains(tokens.get(i - 1))) && (tokens.get(i).equals(";"))) {
                        System.out.println("第" + i + "个token字出现错误,缺少运算数");
                        flag2 = false;
                    }
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
class Production{
    String left;
    List<String> rights;

    Production(String left, List<String> rights) {
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

    @Override
    public int hashCode() {
        return left.length()+rights.size();
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
    Item(Production production, int loc) {
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

    @Override
    public int hashCode() {
        return this.len+this.loc+production.rights.size();
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
    Cluster() {
        items=new HashSet<>();
        nexts=new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Cluster)){
            return false;
        }
        return clusterID==((Cluster)o).clusterID;
    }


    public void add(Item item){
        items.add(item);
    }
    boolean contains(Item item){
        return items.contains(item);
    }
}

