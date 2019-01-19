package haven.res.lib.tree;

import haven.*;

public class Tree extends StaticSprite {
    private final Location scale;
    public final float fscale;
    Message sdt;

    public Tree(Owner owner, Resource res, float scale, Message std) {
        super(owner, res, std);
        this.fscale = scale;

        if (Config.bonsai && scale > 0.6)
            this.scale = mkscale(0.6f);
        else if (scale == 1.0F)
            this.scale = null;
        else
            this.scale = mkscale(scale);
    }

    private static Message invert(Message var0) {
        int var1 = 0;

        int var2;
        for(var2 = 0; !var0.eom(); var2 += 8) {
            var1 |= var0.uint8() << var2;
        }

        var2 = -1 & ~var1;
        MessageBuf var3 = new MessageBuf();
        var3.addint32(var2);
        return new MessageBuf(var3.fin());
    }

    public Tree(Owner owner, Resource res, Message std) {
        this(owner, res, std.eom() ? 1.0F : (float)std.uint8() / 100.0F, invert(std));
        this.sdt = std;
    }
    
    public static Location mkscale(float var0, float var1, float var2) {
        return new Location(new Matrix4f(var0, 0.0F, 0.0F, 0.0F, 0.0F, var1, 0.0F, 0.0F, 0.0F, 0.0F, var2, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F));
    }

    public static Location mkscale(float scale) {
        return mkscale(scale, scale, scale);
    }

    public boolean setup(RenderList var1) {
        if (this.scale != null) {
            var1.prepc(this.scale);
            var1.prepc(States.normalize);
        }

        return super.setup(var1);
    }
}
