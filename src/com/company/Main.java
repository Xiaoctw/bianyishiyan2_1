package com.company;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        new GrammaticalAnalysis();
    }
}

/**
 * 产生式,代表文法当中一个个产生式
 */
class GrammaticalAnalysis{
    List<Production> productions;//产生式
    Map<String,HashSet<String>> FIRST;//首符号集
    Set<String> table;//符号表
    Set<String> nonTermimals;//非终结符
    List<Cluster> clusters;//聚簇
    public GrammaticalAnalysis() {
        productions=new ArrayList<>();
        FIRST=new HashMap<>();
        table=new HashSet<>();
        nonTermimals=new HashSet<>();
        InitProductions();
        for (String s : nonTermimals) {
            findFirsts(s);//获得了first集
        }

    }
    private void InitProductions(){
        table.addAll(Arrays.asList(";","int","double","float","id","=","+","-","*","/","(",")","num","if","then","else","do","while"));
        nonTermimals.addAll(Arrays.asList("P","D1","S1","D","S1","S","T","L","E","F"));
        productions.add(new Production("P", Arrays.asList("D1","S1")));
        productions.add(new Production("D1",Arrays.asList("D1","D",";")));
        productions.add(new Production("D1", Collections.singletonList("")));//空产生式用""表示
        productions.add(new Production("S1",Arrays.asList("S1","S",";")));
        productions.add(new Production("S1", Collections.singletonList("")));
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
                    if(f.equals(s)||f.equals("")){
                        continue;
                    }
                    if(table.contains(f)){//是个终结符
                    FIRST.get(s).add(f);
                    break;
                    }else {//是个非终结符
                        if (!FIRST.containsKey(f)) {
                            findFirsts(f);
                        }
                        FIRST.get(s).addAll(FIRST.get(f));
                        if (!productions.contains(new Production(f, Collections.singletonList("")))) {
                            break;
                        }
                    }
                }
            }
        }
    }
    private void getClusters(Production production){
        Cluster cluster=new Cluster();
        int i=0;
        Production temP=production;
        while (true) {
            Item item = new Item(temP, i);
            if(item.equals(productions.get(0))) {
                item.stringSet.add("#");//加上非终结符
            }
            cluster.add(item);
            for (Production p : productions) {
                if (p.left.equals(item)){

                }
            }
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
    Production pro;//代表对应的产生式
    int len;//产生式右部的长度
    int loc;//点的位置,从0-len
    Set<String> stringSet;
    public Item(Production pro,int loc) {
        this.pro = pro;
        len=pro.rights.size();
        this.loc=loc;
        stringSet=new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Item)){
            return false;
        }
        return this.pro.equals(((Item) o).pro)&&this.loc==((Item) o).loc;
    }
}

//一个聚簇,包含很多个项目
class Cluster{
    Set<Item> items;

    public Cluster(Set<Item> items) {
        this.items = items;
    }

    public Cluster() {
        items=new HashSet<>();
    }
    public void add(Item item){
        items.add(item);
    }
}
