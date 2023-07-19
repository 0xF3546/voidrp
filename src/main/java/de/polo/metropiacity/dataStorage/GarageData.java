package de.polo.metropiacity.dataStorage;

import org.bukkit.World;

public class GarageData {
        private int id;
        private String name;
        private int x;
        private int y;
        private int z;
        private World welt;
        private float yaw;
        private float pitch;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public World getWelt() {
            return welt;
        }

        public void setWelt(World welt) {
            this.welt = welt;
        }

        public float getYaw() {
            return yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public float getPitch() {
            return pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

}
