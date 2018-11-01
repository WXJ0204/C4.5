/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wxj.Tree;

import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public class ClassE implements Cloneable{
    ArrayList<ArrayList<String>> E;

    public ClassE(ArrayList<ArrayList<String>> E) {
        this.E = E;
    }


    public ArrayList<ArrayList<String>> getE() {
        return E;
    }

    public void setE(ArrayList<ArrayList<String>> E) {
        this.E = E;
    }

    @Override//重写clone方法，使得对象可以被复制
    protected Object clone() throws CloneNotSupportedException {
        return (ClassE)super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
