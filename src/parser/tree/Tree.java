package parser.tree;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by HoseinGhahremanzadeh on 5/5/2017.
 */
public class Tree {
    public LinkedList<Tree> children = new LinkedList<>();
    public Tree parent;
    public String head;

    public Tree(String head) {
        this.head = head;
    }

    public char[][] draw() {
        ArrayList<char[][]> chars = new ArrayList<>(children.size());
        ArrayList<Integer> mids = new ArrayList<>(children.size());
        int maxHeight = -1;
        int width = 0;
        int currentWidth = 0;
        int realWidth;
        for (Tree child : children) {
            char[][] last = child.draw();
            chars.add(last);
            if (maxHeight == -1 || (maxHeight < last.length))
                maxHeight = last.length;
            width += last[0].length;
            mids.add(currentWidth);
            currentWidth += last[0].length;
        }
        realWidth = width;
        width = Math.max(width, head.length() + 2);
        maxHeight += 5;
        char[][] result = new char[maxHeight][width];
        for (int i =0;i<maxHeight; i++)
            for (int j =0; j<width;j++)
                result[i][j] = ' ';


        for (int i = width / 2 - head.length() / 2; i < width / 2 + head.length() - head.length() / 2; i++)
            result[0][i] = head.charAt(i - width / 2 + head.length() / 2);


        for (int i = 0; i < children.size(); i++)
            for (int w = 0; w < chars.get(i)[0].length; w++) {
                if (!((i==0 && w<chars.get(i)[0].length/2) || (i==(children.size() - 1) && w>chars.get(i)[0].length/2)))
                    result[2][w + mids.get(i) + width/2 - realWidth / 2] = '_';
                for (int j = 0; j < chars.get(i).length; j++)
                    result[j + 5][mids.get(i) + w + width/2 - realWidth / 2] = chars.get(i)[j][w];
            }

        for (int i = 0; i < children.size(); i++) {
            result[3][mids.get(i) + chars.get(i)[0].length/2 + width/2 - realWidth / 2] = '|';
            result[4][mids.get(i) + chars.get(i)[0].length/2 + width/2 - realWidth / 2] = '|';
        }
        if (children.size() !=0) {
            result[1][width / 2] = '|';
            result[2][width / 2] = '|';
        }
        return result;
    }
}
