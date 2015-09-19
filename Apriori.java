package apriori;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;


public class Apriori {

    int Tlim,Ilim,minsupport;
    boolean DatasetTable [][];
    File f;
    HashMap<HashSet<Integer>,Integer>k_frequentItemSet=null;


    Apriori(int Tlim,int Ilim,int minsupport,File f){
        this.Tlim=Tlim;
        this.Ilim=Ilim;
        this.minsupport=minsupport;
        this.f=f;
        DatasetTable =new boolean[Tlim][Ilim];
        k_frequentItemSet=new HashMap<HashSet<Integer>,Integer>();
    }
    
    void CreateRandomDataset() throws IOException{
       int n; 
       Scanner s=new Scanner(System.in);       
       Vector<Integer> v=new Vector();
        FileWriter fw=null;
        
        try{
            fw=new FileWriter(f);
        }
        catch(IOException e){
            System.out.println("Problem opening file");
            System.exit(-1);
        }
        
        fw.write("TID\tList Of ItemIDs\n");
        for(int i=0;i<Tlim;i++){
            fw.write("T"+i+"\t");
            
            v.clear();
            System.out.println("Enter no of items for T "+ i);
            n=s.nextInt();
            int j=0;
            while(j<n){
                int t=(int)(Math.random()*Ilim);
                if(!v.contains(t)){
                    v.add(t);
                    j++;
                }    
            }
            Collections.sort(v);
            System.out.println("Size"+v.size());
                    
            for(int iter=0;iter<v.size();iter++)
                fw.write("I"+v.get(iter)+" ");
                        
            fw.write("\n");
            //fw.flush();
        }
        fw.close();
    }
    
    void ReadDatasetFile() throws FileNotFoundException,IOException{
       FileReader fr=new FileReader(f);
       for(int i=0;i<Tlim;i++)
        for(int j=0;j<Ilim;j++)
            DatasetTable[i][j]=false;


        while(fr.read()!='\n');
        int ch=fr.read();
        int transno=0,itemno=0;

        while(ch!=-1){
            if(ch=='\n')
                transno++;
            if((char)ch=='I'){
                itemno=0;
                while((char)ch!=' '){
                    int c=fr.read();
                    itemno=itemno*10+c-48;
                    ch=fr.read();
                }
                DatasetTable[transno][itemno]=true;
            }
            ch=fr.read();
        }

        System.out.println("Table created");
        for(int i=0;i<Tlim;i++)
            {for(int j=0;j<Ilim;j++)
                System.out.print(DatasetTable[i][j]+"   ");
                System.out.println("");
            }
    }

    void onefrequentItemset(){
        for(int i=0;i<Ilim;i++){
            //=======itemset generation=======
            HashSet<Integer> itemset=new HashSet<Integer>();
            itemset.add(i);
            //=======put itemset as key, count not calculated yet=======
            k_frequentItemSet.put(itemset, 0);
            int count=0;
            //count value
            for(int transno=0;transno<Tlim;transno++)
                if(DatasetTable[transno][i])count++;
            k_frequentItemSet.put(itemset, count);
        }
        System.out.println("1-Itemset generated\n"+k_frequentItemSet+"\n");
    }
    
    HashSet<Integer> canJoin(HashSet<Integer> anItemSet,HashSet<Integer> anotherItemSet,int k){
        HashSet<Integer> newItemSet=new  HashSet<Integer>();
        Iterator<Integer> i1=anItemSet.iterator();
        Iterator<Integer>i2=anotherItemSet.iterator();
        int count=0;int n1=0,n2=0;
        while(i1.hasNext()&&i2.hasNext())
        {
            n1=i1.next();n2=i2.next();
            if(n1==n2)
                {
                    newItemSet.add(n1);
                    count++;
                }
            else break;
            
        }
        if(count<(k-2)||n1>n2)
        return null;
        newItemSet.add(n1);
        newItemSet.add(n2);
        return newItemSet;
    }
    

    HashMap<HashSet<Integer>,Integer> generateCandidateItemSet(int k){
        
        Set<HashSet<Integer>> prevItemSets=k_frequentItemSet.keySet();
        HashMap<HashSet<Integer>,Integer>CandidateItemSet=new HashMap<HashSet<Integer>,Integer>();
        HashSet<Integer> newItemSet;
        
        for(HashSet<Integer> anItemSet:prevItemSets){
            for(HashSet<Integer> anotherItemSet:prevItemSets){
                if(!anItemSet.equals(anotherItemSet))
                {
                    newItemSet=canJoin(anItemSet,anotherItemSet,k);
                    if(newItemSet!=null)
                    CandidateItemSet.put(newItemSet, 0);
                }
            }
        }

        return CandidateItemSet;
    }
    
    void setCounts(HashMap<HashSet<Integer>,Integer>CandidateItemSet){
        Set<HashSet<Integer>> currItemSets=CandidateItemSet.keySet();
        boolean flag=true;int count=0;
         for(HashSet<Integer> anItemSet:currItemSets){
             count=0;
             for(int transno=0;transno<Tlim;transno++){
                 flag=true;
                 Iterator<Integer> items=anItemSet.iterator();
                 while(items.hasNext()){
                     int itemno=items.next();
                     if(!DatasetTable[transno][itemno])
                         flag=false;
                 }
                 if(flag)count++;
             }
             CandidateItemSet.put(anItemSet, count);
         }
        
    }
    
    HashMap<HashSet<Integer>,Integer> prune(HashMap<HashSet<Integer>,Integer>CandidateItemSet){
        Set<Entry<HashSet<Integer>,Integer>> EntrySet= CandidateItemSet.entrySet();
        HashMap<HashSet<Integer>,Integer> finalset=new HashMap<HashSet<Integer>,Integer>();
        for(Map.Entry<HashSet<Integer>,Integer> entry:EntrySet){
            if(entry.getValue()>=minsupport)
                finalset.put(entry.getKey(), entry.getValue());
        }
        return finalset;
    }

    void generateFrequentItemSet(int k){
        HashMap<HashSet<Integer>,Integer>CandidateItemSet=generateCandidateItemSet(k);
        System.out.println(k+ " -Itemsets\n"+CandidateItemSet);
        setCounts(CandidateItemSet);
        System.out.println(k+ " -Itemsets , After setting counts\n"+CandidateItemSet);
        k_frequentItemSet=prune(CandidateItemSet);
        System.out.println(k+" -Frequent Itemset, After Pruning\n"+k_frequentItemSet+"\n\n");
    }

    
    public static void main(String[] args) throws IOException {
        int Tlim,Ilim,minsupport=2;
        File f=new File("dataset.txt");
        Scanner s=new Scanner(System.in);       
        System.out.println("Enter no of transactions");
        Tlim=s.nextInt();
        System.out.println("Enter no of items");
        Ilim=s.nextInt();
        //Initialisation Completed

        Apriori a=new Apriori(Tlim,Ilim,minsupport,f);
        a.CreateRandomDataset();//write a random dataset into file
        a.ReadDatasetFile();//store transaction details into 2-D array "DatasetTable "
        a.onefrequentItemset();
        
        for(int i=2;i<Ilim;i++)
            a.generateFrequentItemSet(i);
    }
    
}
