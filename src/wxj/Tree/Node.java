/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wxj.Tree;

import java.util.ArrayList;

/**
 *决策树的节点
 * @author WXJ
 */
public class Node {
    String lable;//当Node为叶节点时，将剩下的属性投票选出最多的属性，赋给lable
    ArrayList<Node> Child;//孩子节点
    String[] test_cond;//0，1，2分别记录：当前节点的分类属性，分类点，分类值
}
