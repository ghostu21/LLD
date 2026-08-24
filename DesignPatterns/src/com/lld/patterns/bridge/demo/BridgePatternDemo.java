package com.lld.patterns.bridge.demo;

import com.lld.patterns.bridge.breathing.GillBreathing;
import com.lld.patterns.bridge.breathing.LungBreathing;
import com.lld.patterns.bridge.breathing.Photosynthesis;
import com.lld.patterns.bridge.breathing.SkinBreathing;
import com.lld.patterns.bridge.living.Dog;
import com.lld.patterns.bridge.living.Fish;
import com.lld.patterns.bridge.living.Frog;
import com.lld.patterns.bridge.living.LivingThings;
import com.lld.patterns.bridge.living.Tree;
import com.lld.patterns.bridge.living.Whale;

public class BridgePatternDemo {
    public static void main(String[] args) {
        System.out.println("======= Bridge Design Pattern - Solution Demo ======");

        LivingThings dog = new Dog(new LungBreathing());
        LivingThings fish = new Fish(new GillBreathing());
        LivingThings tree = new Tree(new Photosynthesis());
        LivingThings whale = new Whale(new LungBreathing());
        LivingThings frog = new Frog(new SkinBreathing());

        dog.breathe();
        fish.breathe();
        tree.breathe();
        whale.breathe();
        frog.breathe();
    }
}
