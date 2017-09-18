package uk.co.staticvoid.gliderrider.business;

import org.bukkit.Material;
import uk.co.staticvoid.gliderrider.domain.Location;
import uk.co.staticvoid.gliderrider.helper.LocationHelper;

public class BlockEditor {

    public void changeBlockMaterial(Location location, Material material) {
        LocationHelper.toBukkitLocation(location).getBlock().setType(material);
    }
}
