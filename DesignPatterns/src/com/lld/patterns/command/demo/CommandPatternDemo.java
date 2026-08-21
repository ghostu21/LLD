package com.lld.patterns.command.demo;

import com.lld.patterns.command.ac.AirConditioner;
import com.lld.patterns.command.ac.SetTemperatureCommand;
import com.lld.patterns.command.ac.TurnOffCommand;
import com.lld.patterns.command.ac.TurnOnCommand;
import com.lld.patterns.command.bulb.Bulb;
import com.lld.patterns.command.bulb.BulbOnCommand;
import com.lld.patterns.command.invoker.RemoteController;

public class CommandPatternDemo {
    public static void main(String[] args) {
        System.out.println("##### Command Pattern: Solution Demo #####");

        AirConditioner airConditioner = new AirConditioner();
        RemoteController remote = new RemoteController();

        remote.setCommand(new TurnOnCommand(airConditioner));
        remote.pressButton();
        remote.setCommand(new SetTemperatureCommand(airConditioner, 25));
        remote.pressButton();
        remote.setCommand(new SetTemperatureCommand(airConditioner, 18));
        remote.pressButton();
        remote.setCommand(new TurnOffCommand(airConditioner));
        remote.pressButton();

        remote.undo();
        remote.undo();
        remote.undo();
        remote.undo();

        System.out.println();
        System.out.println("##### Same invoker, new receiver (Bulb) — remote class unchanged #####");
        remote.setCommand(new BulbOnCommand(new Bulb()));
        remote.pressButton();
        remote.undo();
    }
}
