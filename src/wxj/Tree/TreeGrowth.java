/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wxj.Tree;

import com.csvreader.CsvReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Math.log;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class TreeGrowth {

    String path;
    ClassE classE;
    ClassF classF;
    String povi;

    public static void main(String[] args) throws IOException {
        TreeGrowth tree1 = new TreeGrowth("src/Iris.csv", "Iris-setosa");
        TreeGrowth tree2 = new TreeGrowth("src/Iris.csv", "Iris-versicolor");
        TreeGrowth tree3 = new TreeGrowth("src/Iris.csv", "Iris-virginica");
        System.out.println("wxj.Tree.TreeGrowth.main()");
    }

    public TreeGrowth(String path, String povi) throws FileNotFoundException, IOException {
        this.path = path;
        this.povi = povi;
        this.classE = new ClassE(new ArrayList<>());

        CsvReader reader = new CsvReader(path, ',', Charset.forName("UTF-8"));
        reader.readRecord();
        String[] header = reader.getValues();//第一行
        int len = header.length;//列数
        ArrayList<String> lineF = new ArrayList<>();
        for (int i = 0; i < len - 1; i++) {
            lineF.add(header[i]);
        }
        this.classF = new ClassF(lineF);

        while (reader.readRecord()) {
            reader.getCurrentRecord();//读取当前位置
            reader.getRawRecord();//读取行
            ArrayList<String> line = new ArrayList<>();
            for (int row = 0; row < len; row++) {
                line.add(reader.getValues()[row]);
            }
            this.classE.E.add(line);

        }

        Node root = StartBuiltTree(classE, classF);
        System.out.println("wxj.Tree.TreeGrowth.<init>()");
    }

    public Node StartBuiltTree(ClassE E, ClassF F) {
        if (stopping_cond(E, F) == true) {
            Node leaf = new Node();
            leaf.lable = classify(E);
            return leaf;
        } else {
            Node root = new Node();
            root.test_cond = find_best_split(E, F);
            
            
            //分裂E
            //先按最大增益属性来排序
            int valueF = -1;
            for(int i = 0;i<F.line.size();i++){
                if(F.line.get(i).equals(root.test_cond[0]))
                    valueF = i;
            }
            //System.out.println(valueF);
            eSort(E, valueF);
            
        ClassE eleft = null;//复制对象
        try {
            eleft = (ClassE) E.clone();
            eleft.E = spiltArrayList(E.E, 0, Integer.parseInt(root.test_cond[1]));//ArrayList也要复制...
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(TreeGrowth.class.getName()).log(Level.SEVERE, null, ex);
        }        
        for(int i =0;i<Integer.parseInt(root.test_cond[1]);i++){
            E.E.remove(0);
        }
        root.Child = new ArrayList<>();
        
        //小于test_cond[3]的分到左孩子
        Node childleft = StartBuiltTree(eleft,  F);
        root.Child.add(childleft);
        
        //大于test_cond[3]的分到右孩子
        Node childright = StartBuiltTree(E, F);
        root.Child.add(childright);
        return root;
        }
        

    }

    private String[] find_best_split(ClassE E, ClassF F) {
        ClassE e = null;//复制对象，以免排序给排乱了
        try {
            e = (ClassE) E.clone();
            e.E = spiltArrayList(E.E, 0, E.E.size());//ArrayList也要复制...
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(TreeGrowth.class.getName()).log(Level.SEVERE, null, ex);
        }
        String res[] = new String[3];//记录返回的属性名和分裂点
        //计算InfoD
        String[] key = new String[E.E.size()];
        for (int i = 0; i < E.E.size(); i++) {
            key[i] = new String(E.E.get(i).get(E.E.get(i).size() - 1));
        }
        float infoD = InfoD(key);
        float info;
        //计算每个属性的最佳分裂点,假设都是连续属性
        float[] value = new float[F.line.size()];
        int[] pos = new int[F.line.size()];
        String[] posValue = new String[F.line.size()];
        
        for (int i = 0; i < F.line.size(); i++) {
            //先排序,按第i个属性排序
            eSort(e, i);
            float maxGainRatio = -1000;
           // System.out.println("wxj.Tree.TreeGrowth.find_best_split()");
            for (int point = 1; point < e.E.size(); point++) {
                //计算分子： (infoD-infoA)/
                float a = infoD - InfoA(e, point);
                //计算分母： 
                float b = SplitInfoA(e,point);
                //计算增益率
                float gain_ratio = a/b;
                if(gain_ratio>maxGainRatio){
                    maxGainRatio = gain_ratio;
                    pos[i]=point;
                    posValue[i]=e.E.get(point).get(i);
                }
                //System.out.println(gain_ratio);
            }
            System.out.println(maxGainRatio);
            value[i] = maxGainRatio;
            //System.out.println("wxj.Tree.TreeGrowth.find_best_split()");

        }
        //返回最大增益率的值
        float max = value[0];
        int num = 0;
        for(int i = 1;i<F.line.size();i++){
        if(max<value[i])
            max = value[i];
        num = i;
    }
        res[0] = F.line.get(num);
        res[1] = new Integer(pos[num]).toString();
       res[2] = posValue[num];
        return res;
        
//     int k = 0;
//     float[] value = new float[E.E.size()];
//     //String[] key = new String[E.E.size()];
//     for(int i = 0;i<E.E.size();i++){
//         value[i] = Float.parseFloat(E.E.get(i).get(k));
//         key[i] = new String(E.E.get(i).get(E.E.get(i).size()-1));
//     }
       
    }

    private ArrayList<ArrayList<String>> spiltArrayList(ArrayList<ArrayList<String>> list, int i, int j) {
        ArrayList<ArrayList<String>> resList = new ArrayList<>();
        for (int k = i; k < j; k++) {
            ArrayList<String> line = new ArrayList<>();
            for (int n = 0; n < list.get(0).size(); n++) {
                line.add(list.get(k).get(n));
            }
            resList.add(line);
        }
        return resList;

    }

    private void eSort(ClassE e, int i) {

        quickSort(e, i, 0, e.E.size() - 1);

    }

    static public void quickSort(ClassE e, int k, int left, int right) {
        if (left >= right) {
            return;  //数据源少于两个数，就返回
        }
        int i = left;  //用于确定基准位的下标
        for (int j = left; j < right; j++) { //从左至右遍历，找出所有比基准值小的数
            if (Float.parseFloat(e.E.get(j).get(k)) < Float.parseFloat(e.E.get(right).get(k))) {         //以最右边的数为基准值
                swap(e, i++, j);
            }
        }
        swap(e, i, right);    //让基准数归位
        quickSort(e, k, left, i - 1);
        quickSort(e, k, i + 1, right);
    }

    static private void swap(ClassE e, int i, int j) {  //交换i j的位置
        ArrayList<String> temp = new ArrayList<>();
        //temp=j
        for (int n = 0; n < e.E.get(j).size(); n++) {
            temp.add(e.E.get(j).get(n));
        }

        //j=i
        for (int n = 0; n < e.E.get(i).size(); n++) {
            e.E.get(j).set(n, e.E.get(i).get(n));
        }

        //i=j
        for (int n = 0; n < e.E.get(i).size(); n++) {
            e.E.get(i).set(n, temp.get(n));
        }

    }

    private float SplitInfoA(ClassE e, int point){
        int leftProv = 0;
        int rightProv = 0;
        double info;
        for (int i = 0; i < e.E.size(); i++) {
            if (i < point && e.E.get(i).get(e.E.get(0).size() - 1).equals(povi)) {
                leftProv++;
            }
            if (i >= point && e.E.get(i).get(e.E.get(0).size() - 1).equals(povi)) {
                rightProv++;
            }
        }

        if (leftProv != 0 && rightProv != 0) {
            info = -(log(((double) leftProv / (double) point)) / log(2)) - (log((double) rightProv / (double) (e.E.size() - point)) / log(2));
        } else if (leftProv == 0 && rightProv == 0) {
            info = 0;
        } else if (leftProv == 0) {
            info = -(log( ((double)rightProv / (double)(e.E.size() - point))) / log(2));
        } else {
            info = -(log( ((double)leftProv / (double)point)) / log(2));
        }

        if(info==0){
            System.out.println("wxj.Tree.TreeGrowth.SplitInfoA()");
        }

        return new BigDecimal(info).floatValue();
    }
    
    
    private float InfoA(ClassE e, int point) {//point:1~size()
        int leftProv = 0;
        int rightProv = 0;
        double info;
        for (int i = 0; i < e.E.size(); i++) {
            if (i < point && e.E.get(i).get(e.E.get(0).size() - 1).equals(povi)) {
                leftProv++;
            }
            if (i >= point && e.E.get(i).get(e.E.get(0).size() - 1).equals(povi)) {
                rightProv++;
            }
        }

        if (leftProv != 0 && rightProv != 0) {
            info = -(((double) leftProv / (double) point)) * (log((double) ((double) leftProv / (double) point)) / log(2)) - ((double) ((double) rightProv / (double) (e.E.size() - point))) * (log((double) ((double) rightProv / (double) (e.E.size() - point))) / log(2));
        } else if (leftProv == 0 && rightProv == 0) {
            info = 0;
        } else if (leftProv == 0) {
            info = -(((double)rightProv / (double)(e.E.size() - point))) * (log(((double)rightProv / (double)(e.E.size() - point))) / log(2));
        } else {
            info = -( ((double)leftProv / (double)point)) * (log( ((double)leftProv / (double)point)) / log(2));
        }



        return new BigDecimal(info).floatValue();
    }

    private float InfoD(String[] data) {
        //计算InfoD
        int[] count = new int[2];
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals(povi)) {
                count[0]++;
            } else {
                count[1]++;
            }
        }
        double len = (double) count[0] + (double) count[1];
        double p1 = (double) count[0];
        double p2 = (double) count[1];
        double info;
        if(p1!=0&&p2!=0)
        info = -(p1 / len) * (log(p1 / len) / log(2)) - (p2 / len) * (log(p2 / len) / log(2));
        else if(p1==0)
            info = - (p2 / len) * (log(p2 / len) / log(2));
        else if(p2==0)
            info = -(p1 / len) * (log(p1 / len) / log(2));
        else 
            info = 0;
        
        
        return new BigDecimal(info).floatValue();
    }

    private String classify(ClassE E) {
        int len = E.E.get(0).size();
        int count = 0;
        for (int i = 0; i < E.E.size(); i++) {
            if (E.E.get(i).get(len - 1).equals(povi)) {
                count++;
            }
        }
        if (2 * count > E.E.size()) {
            return povi;
        } else {
            return "other";
        }
    }

    private boolean stopping_cond(ClassE classE, ClassF classF) {

        
        
        int len = classE.E.get(0).size();
        int count = 0;
        for (int i = 0; i < classE.E.size(); i++) {
            if (classE.E.get(i).get(len - 1).equals(povi)) {
                count++;
            }
        }
        if (count == classE.E.size()||count <=1) {
            return true;
        } else {
            return false;
        }
    }

}
