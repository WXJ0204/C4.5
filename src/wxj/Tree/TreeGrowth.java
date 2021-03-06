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
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *基于C4.5的决策树（目前只支持数值型）
 * @author WXJ
 */
public class TreeGrowth {

    String path;//测试集的文件路径
    ClassE classE;//测试集
    ClassF classF;//属性集
    String povi;//正例
    Node root;//决策树的根

    public static void main(String[] args) throws IOException {
        //因为有三种三类，故建立三棵决策树，每一棵以一个属性为正例，其余属性为负例
        TreeGrowth tree1 = new TreeGrowth("src/Iris.csv", "Iris-setosa");
        TreeGrowth tree2 = new TreeGrowth("src/Iris.csv", "Iris-versicolor");
        TreeGrowth tree3 = new TreeGrowth("src/Iris.csv", "Iris-virginica");

        Node[] node = new Node[3];
        node[0] = tree1.root;
        node[1] = tree2.root;
        node[2] = tree3.root;
        TestTree("src/test.csv", node);
       
    }

    public static void TestTree(String path, Node[] root) throws FileNotFoundException, IOException {
        CsvReader reader = new CsvReader(path, ',', Charset.forName("UTF-8"));
        reader.readRecord();
        String[] header = reader.getValues();//第一行
        int len = header.length;//列数
        ArrayList<String> result = new ArrayList<>();//记录结果
        ArrayList<String> testData = new ArrayList<>();//记录原数据
        int line = 0;//记录结果的行数
        int correct = 0;//正确的测试结果

        //f_num记录 属性-序号 的键值对
        Map<String, Integer> f_num = new HashMap<>();
        for (int i = 0; i < header.length - 1; i++) {
            f_num.put(header[i], i);
        }

        while (reader.readRecord()) {
            testData.add(reader.getValues()[len-1]);//记录结果
            //为数值型：
            float[] cmp = new float[header.length - 1];//cmp[i]记录第i项属性的值
            for (int i = 0; i < header.length - 1; i++) {
                cmp[i] = Float.parseFloat(reader.getValues()[i]);
            }
            Node node = new Node();//节点指针

            for (int m = 0; m < root.length; m++) {//这个循环是将测试项逐个放入决策树中进行测试，直到分类到某个属性
                node = root[m];
                while (null == node.lable) {
                    int fNum = f_num.get(node.test_cond[0]);
                    float value = Float.parseFloat(node.test_cond[2]);
                    if (cmp[fNum] <= value) {
                        node = node.Child.get(0);
                    } else {
                        node = node.Child.get(1);
                    }
                }
                if (!"other".equals(node.lable)) {//成功被分类，跳出循环
                    break;
                }
            }
            if ("other".equals(node.lable)) {//三次后还没有被分类，则只能赋出现最多的属性给它
                node.lable = "Iris-versicolor";
            }
            result.add(node.lable);//存放结果
            System.out.println("实际分类："+  reader.getValues()[len-1]  +"  "  +  "决策树输出的结果："+ node.lable);
            line++;
        }
        
        //比较准确率
        for(int i = 0;i<line;i++){
            if(result.get(i).equals(testData.get(i)))
                correct++;
        }
        System.out.println("准确率："+(double)correct/(double)line);

    }

    public TreeGrowth(String path, String povi) throws FileNotFoundException, IOException {
        this.path = path;
        this.povi = povi;
        this.classE = new ClassE(new ArrayList<>());

        //读取文件并生成测试集和属性集
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
        
        //使用训练集E和属性集F建立树      
        this.root = StartBuiltTree(classE, classF);
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
            //找到最大增益属性
            int valueF = -1;
            for (int i = 0; i < F.line.size(); i++) {
                if (F.line.get(i).equals(root.test_cond[0])) {
                    valueF = i;
                }
            }
            //将E按最大增益属性来排序
            eSort(E, valueF);

            //以最大增益属性的最佳分裂点为界，将E的一部分复制到eleft中，再删掉这些部分，就使E分裂成了两个子集
            ClassE eleft = null;//复制对象
            try {
                eleft = (ClassE) E.clone();
                eleft.E = spiltArrayList(E.E, 0, Integer.parseInt(root.test_cond[1]));//ArrayList也要复制...
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(TreeGrowth.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int i = 0; i < Integer.parseInt(root.test_cond[1]); i++) {
                E.E.remove(0);
            }
            root.Child = new ArrayList<>();

            //小于分裂点值的训练集分到左孩子
            Node childleft = StartBuiltTree(eleft, F);
            root.Child.add(childleft);

            //大于分裂点值的训练集分到右孩子
            Node childright = StartBuiltTree(E, F);
            root.Child.add(childright);
            return root;
        }

    }

    //从训练集E和属性F中，找到信息增益率最大的属性，因为是数值属性，还要给出分裂点
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
            key[i] = E.E.get(i).get(E.E.get(i).size() - 1);
        }
        float infoD = InfoD(key);
        
        //计算每个属性的最佳分裂点,假设都是连续属性
        float[] value = new float[F.line.size()];//保存每个属性的最大信息增益率
        int[] pos = new int[F.line.size()];//保存每个属性的最大信息增益率的分裂点
        String[] posValue = new String[F.line.size()];//保存每个属性的最大信息增益率的分裂点的值

        for (int i = 0; i < F.line.size(); i++) {
            //先排序,按第i个属性排序
            eSort(e, i);
            float maxGainRatio = -1000;
            for (int point = 1; point < e.E.size(); point++) {
                //计算分子： (infoD-infoA)，即信息熵
                float a = infoD - InfoA(e, point);
                //计算分母： 
                float b = SplitInfoA(e, point);
                //System.out.println(b);
                //计算本次的信息增益率
                float gain_ratio = a / b;

                if (gain_ratio > maxGainRatio) {//若本次信息增益率大于原有的最大信息增益率，则取本次的值
                    maxGainRatio = gain_ratio;
                    pos[i] = point;
                    posValue[i] = e.E.get(point).get(i);
                }
            }

            value[i] = maxGainRatio;//保存每个属性的最大信息增益率

        }
        //返回最大增益率的值
        float max = value[0];
        int num = 0;
        for (int i = 1; i < F.line.size(); i++) {
            if (max < value[i]) {
                max = value[i];
                num = i;
            }
        }
        res[0] = F.line.get(num);
        res[1] = Integer.toString(pos[num]);
        res[2] = posValue[num];
        return res;

    }

    //从原有的ArrayList中分裂出一个新的ArrayList，分裂范围为[i,j)
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

    //将训练集E按第i个属性排序
    private void eSort(ClassE e, int i) {
        quickSort(e, i, 0, e.E.size() - 1);

    }
     
    //快速排序，仅给eSort调用
    static private void quickSort(ClassE e, int k, int left, int right) {
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

    //交换训练集e中i和j项属性
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

    //计算训练集e中，将point作为分裂点，所得新的信息需求
    private float SplitInfoA(ClassE e, int point) {
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
        } else {
            info = 1000;
        }

        return new BigDecimal(info).floatValue();
    }

    //计算信息增益率的分母,以point为分裂点
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
            info = -(((double) rightProv / (double) (e.E.size() - point))) * (log(((double) rightProv / (double) (e.E.size() - point))) / log(2));
        } else {
            info = -(((double) leftProv / (double) point)) * (log(((double) leftProv / (double) point)) / log(2));
        }

        return new BigDecimal(info).floatValue();
    }

    //计算信息熵
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
        if (p1 != 0 && p2 != 0) {
            info = -(p1 / len) * (log(p1 / len) / log(2)) - (p2 / len) * (log(p2 / len) / log(2));
        } else if (p1 == 0) {
            info = -(p2 / len) * (log(p2 / len) / log(2));
        } else if (p2 == 0) {
            info = -(p1 / len) * (log(p1 / len) / log(2));
        } else {
            info = 0;
        }

        return new BigDecimal(info).floatValue();
    }

    //返回训练集E中，个数最多的结果值，用于叶节点的投票
    private String classify(ClassE E) {
        int len = E.E.get(0).size();
        int count = 0;
        for (int i = 0; i < E.E.size(); i++) {
            if (E.E.get(i).get(len - 1).equals(povi)) {
                count++;
            }
        }
        if (2 * count >= E.E.size()) {
            return povi;
        } else {
            return "other";
        }
    }

    //决定是否终止递归树的增长
    private boolean stopping_cond(ClassE classE, ClassF classF) {
        int len = classE.E.get(0).size();
        int count = 0;
        for (int i = 0; i < classE.E.size(); i++) {
            if (classE.E.get(i).get(len - 1).equals(povi)) {
                count++;
            }
        }
        if (count == classE.E.size() || count <= 4) {
            return true;
        } else {
            return false;
        }
    }

}
