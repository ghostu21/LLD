package com.lld.patterns.composite.demo;

import com.lld.patterns.composite.expression.ArithmeticExpression;
import com.lld.patterns.composite.expression.Expression;
import com.lld.patterns.composite.expression.NumberOperand;
import com.lld.patterns.composite.expression.OperationType;
import com.lld.patterns.composite.filesystem.Directory;
import com.lld.patterns.composite.filesystem.File;

public class CompositePatternDemo {
    public static void main(String[] args) {
        runFileSystem();
        runArithmetic();
    }

    private static void runFileSystem() {
        System.out.println("======= Composite Design Pattern ======");
        System.out.println("======= Example: File Structure ======");

        File receipt = new File("receipt.pdf");
        File invoice = new File("invoice.pdf");
        File torrentLinks = new File("torrentLinks.txt");
        File tomCruise = new File("tomCruise.jpg");
        File dumbAndDumber = new File("DumbAndDumber.mp4");
        File hangoverI = new File("HangoverI.mp4");

        Directory moviesDirectory = new Directory("Movies");
        Directory comedyMovieDirectory = new Directory("ComedyMovies");

        moviesDirectory.add(receipt);
        moviesDirectory.add(invoice);
        moviesDirectory.add(torrentLinks);
        moviesDirectory.add(tomCruise);
        moviesDirectory.add(comedyMovieDirectory);
        comedyMovieDirectory.add(dumbAndDumber);
        comedyMovieDirectory.add(hangoverI);

        moviesDirectory.printContents();
        System.out.println();
    }

    private static void runArithmetic() {
        System.out.println("======= Example: Arithmetic Expressions ======");
        System.out.println("2 * (1 + 7)");

        ArithmeticExpression two = new NumberOperand(2);
        ArithmeticExpression one = new NumberOperand(1);
        ArithmeticExpression seven = new NumberOperand(7);

        ArithmeticExpression addExpression = new Expression(one, seven, OperationType.ADD);
        ArithmeticExpression parentExpression = new Expression(two, addExpression, OperationType.MULTIPLY);

        System.out.println(parentExpression.evaluate());
    }
}
