package block;

import main.Location;

import java.awt.*;

public class BlockSolid extends Block {

    public BlockSolid(BlockTypes type, Location loc) {
        super(type, loc);
    }

    @Override
    public Color getColor() {
        return type.getColor();
    }

    @Override
    public boolean isCollidable() {
        return true;
    }
}
