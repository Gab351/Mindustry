package io.anuke.mindustry.world.blocks.types.defense;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.anuke.mindustry.Vars;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Draw;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Hue;
import io.anuke.ucore.util.Angles;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Strings;

public class RepairTurret extends PowerTurret{

	public RepairTurret(String name) {
		super(name);
		powerUsed = 0.1f;
	}
	
	@Override
	public void getStats(Array<String> list){
		list.add("[health]health: " + health);
		list.add("[powerinfo]Power Capacity: " + (int)powerCapacity);
		list.add("[powerinfo]Power/shot: " + Strings.toFixed(powerUsed, 1));
		list.add("[turretinfo]Range: " + (int)range);
		list.add("[turretinfo]Repairs/Second: " + Strings.toFixed(60f/reload, 1));
	}
	
	@Override
	public void postInit(){
		description = "[turretinfo]Range: " + (int)range + "\nPower/unit: "+powerUsed+"\n[description]Heals nearby tiles.";
	}
	
	@Override
	public void update(Tile tile){
		PowerTurretEntity entity = tile.entity();
		
		if(entity.power < powerUsed) return;
		
		if(Timers.get(entity, "blocktarget", targetInterval)){
			entity.blockTarget = Vars.world.findTileTarget(tile.worldx(), tile.worldy(), tile, range, true);
		}
		
		if(entity.blockTarget != null){
			float target = entity.angleTo(entity.blockTarget);
			entity.rotation = Mathf.slerp(entity.rotation, target, 0.16f*Timers.delta());

			if(Timers.get(tile, "reload", reload) && Angles.angleDist(target, entity.rotation) < shootCone){
				entity.blockTarget.health++;
				
				if(entity.blockTarget.health > entity.blockTarget.health)
					entity.blockTarget.health = entity.blockTarget.maxhealth;
				
				entity.power -= powerUsed;
			}
		}
	}
	
	@Override
	public void drawPixelOverlay(Tile tile){
		Draw.color("green");
		Draw.dashcircle(tile.worldx(), tile.worldy(), range);
		Draw.reset();
		
		drawPowerBar(tile);
	}
	
	@Override
	public void drawOver(Tile tile){
		TurretEntity entity = tile.entity();
		
		if(entity.blockTarget != null && Angles.angleDist(entity.angleTo(entity.blockTarget), entity.rotation) < 10){
			Tile targetTile = entity.blockTarget.tile;
			Vector2 offset = targetTile.block().getPlaceOffset();
			float x = tile.worldx(), y = tile.worldy();
			float x2 = entity.blockTarget.x + offset.x, y2 = entity.blockTarget.y + offset.y;

			Draw.color(Hue.rgb(138, 244, 138, (MathUtils.sin(Timers.time()) + 1f) / 14f));
			Draw.alpha(0.3f);
			Draw.thickness(4f);
			Draw.line(x, y, x2, y2);
			Draw.thickness(2f);
			Draw.rect("circle", x2, y2, 7f, 7f);
			Draw.alpha(1f);
			Draw.thickness(2f);
			Draw.line(x, y, x2, y2);
			Draw.thickness(1f);
			Draw.rect("circle", x2, y2, 5f, 5f);
			Draw.reset();
		}
		
		Draw.rect(name(), tile.worldx(), tile.worldy(), entity.rotation - 90);
	}
}
