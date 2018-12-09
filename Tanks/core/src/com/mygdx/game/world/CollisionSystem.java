package com.mygdx.game.world;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.sprites.*;
import com.mygdx.game.treasures.Treasure;

import static com.mygdx.game.world.World.MAP_HEIGHT;
import static com.mygdx.game.world.World.MAP_WIDTH;

public class CollisionSystem {
    private World world;
    private boolean multiPlayer;

    private Hero player1;
    private Hero player2;
    private Array<Enemy> enemies;

    private Array<Steel> steelBlocks;
    private Array<Brick> bricks;
    private Fortress fortress;

    private Treasure treasure;

    public CollisionSystem(World world) {
        this.world = world;
        multiPlayer = world.isMultiplayer();

        player1 = world.getPlayer1();

        if (multiPlayer) {
            player2 = world.getPlayer2();
        }

        enemies = world.getEnemies();

        steelBlocks = world.getSteelBlocks();
        bricks = world.getBricks();
        fortress = world.getFortress();

        treasure = world.getTreasure();
    }

    public void update() {
        if (player1.getState() != Tank.State.DESTROYED) {
            verifyHero(player1);
        }
        if (multiPlayer && player2.getState() != Tank.State.DESTROYED) {
            verifyHero(player2);
        }
        verifyEnemies();

        treasure = world.getTreasure();
    }

    private void verifyHero(Hero hero) {
        verifyCollision(hero);
        verifyBrickCollision(hero);
        verifySteelCollision(hero);
        verifyMapBoundsCollision(hero);
        verifyTreasureCollision(hero);
        verifyFortressCollision(hero);

        if (hero.getBullet() != null && hero.getBullet().getState() == Bullet.State.FLYING) {
            Bullet bullet = hero.getBullet();
            verifyBrickCollision(bullet);
            verifySteelCollision(bullet);
            verifyMapBoundsCollision(bullet);
            verifyBulletCollision(bullet);
            verifyFortressCollision(bullet);
            verifyTeammateBulletCollision(bullet);

            if (bullet.getState() != Bullet.State.EXPLODING) {
                verifyEnemyCollision(bullet);
            }
        }
    }

    private void verifyEnemies() {
        for (int i = 0; i < enemies.size; i++) {
            Enemy enemy = enemies.get(i);

            if (enemy.getState() != Tank.State.EXPLODING) {
                verifyCollision(enemy);
                verifyBrickCollision(enemy);
                verifyMapBoundsCollision(enemy);
                verifySteelCollision(enemy);
                verifyFortressCollision(enemy);
            }

            if (enemy.getBullet() != null && enemy.getBullet().getState() == Bullet.State.FLYING) {
                Bullet bullet = enemy.getBullet();

                verifyBrickCollision(bullet);
                verifySteelCollision(bullet);
                verifyMapBoundsCollision(bullet);
                verifyBulletCollision(bullet);
                verifyFortressCollision(bullet);
                verifyHeroCollision(bullet);
            }
        }
    }

    private void verifyMapBoundsCollision(DynamicGameObject dynamicGameObject) {
        int x = (int)dynamicGameObject.getPosition().x;
        int y = (int)dynamicGameObject.getPosition().y;

        int objectWidth = (int)dynamicGameObject.getBounds().getWidth();

        if (x < 16 || y < 16 || x > (MAP_WIDTH + 16) - objectWidth
                || y > (MAP_HEIGHT + 16) - objectWidth) {
            dynamicGameObject.respondMapBoundsCollision();
        }
    }

    private void verifySteelCollision(DynamicGameObject dynamicGameObject) {
        for (Steel steel : steelBlocks) {
            if (dynamicGameObject.getBounds().overlaps(steel.getBounds())) {
                dynamicGameObject.respondSteelCollision();
                break;
            }
        }
    }

    private void verifyBrickCollision(Tank tank) {
        for (Brick brick : bricks) {
            if (tank.getBounds().overlaps(brick.getBounds())) {

                if (tank.getState() != Tank.State.WALL_BREAKING) {
                    tank.respondBrickCollision();
                    break;
                } else {
                    brick.destroy();
                }
            }
        }
    }

    private void verifyCollision(Tank tank) {
        for (Enemy enemy : enemies) {
            verifyCollision(tank, enemy);
        }

        verifyCollision(tank, player1);
        verifyCollision(tank, player2);
    }

    private void verifyCollision(Tank tankOne, Tank tankTwo) {
        if (tankOne != tankTwo && tankOne.getBounds().overlaps(tankTwo.getBounds())) {
            tankOne.respondTankCollision();
        }
    }

    private void verifyTreasureCollision(Hero hero) {
        if (treasure != null && hero.getBounds().overlaps(treasure.getBounds())) {
            switch (treasure.getType()) {
                case ENEMY_KILLER:
                    world.killEnemies();
                    break;

                case TIME_STOPPER:
                    world.stopTime();
                    break;

                case EXTRA_LIFE:
                    hero.addExtraLife();
                    break;

                case WALL_BREAKER:
                    hero.addWallBreakingMod();
                    break;

                case BASE_DEFENDER:
                    world.defendBase();
                    break;

                case TANK_IMPROVER:
                    hero.improve();
                    break;

                case SHIELD:
                    hero.addShield();
                    break;
            }

            treasure.respondTankCollision();
        }
    }

    private void verifyBrickCollision(Bullet bullet) {
        if (bulletCollidesWithBricks(bullet)) {

            for (Brick brick : bricks) {

                if (bullet.getBigBounds().overlaps(brick.getBounds())) {
                    brick.destroy();
                }
            }
            bullet.respondBrickCollision();
        }
    }

    private boolean bulletCollidesWithBricks(Bullet bullet) {
        if (bullet.getState() != Bullet.State.EXPLODING) {
            for (Brick brick : bricks) {
                if (bullet.getBounds().overlaps(brick.getBounds())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void verifyHeroCollision(Bullet bullet) {
        verifyCollision(player2, bullet);
        verifyCollision(player1, bullet);
    }

    private void verifyEnemyCollision(Bullet bullet) {
        for (Enemy enemy : enemies) {
            verifyCollision(enemy, bullet);
        }
    }

    private void verifyCollision(Tank tank, Bullet bullet) {
        if (bullet.getBounds().overlaps(tank.getBounds())) {
            bullet.respondTankCollision();
            tank.respondBulletCollision();
        }
    }

    private void verifyTeammateBulletCollision(Bullet bullet) {
        if (player1.getBullet() != null && player1.getBullet() == bullet) {
            if (bullet.getBounds().overlaps(player2.getBounds())) {
                bullet.respondTankCollision();
                player2.respondTeammateBulletCollision();
            }

        } else if (player2.getBullet() != null && player2.getBullet() == bullet) {
            if (bullet.getBounds().overlaps(player1.getBounds())) {
                bullet.respondTankCollision();
                player1.respondTeammateBulletCollision();
            }
        }
    }

    private void verifyBulletCollision(Bullet bullet) {
        for (Enemy enemy : enemies) {
            verifyBulletCollision(enemy, bullet);
        }

        verifyBulletCollision(player1, bullet);
        verifyBulletCollision(player2, bullet);
    }

    private void verifyBulletCollision(Tank tank, Bullet bullet) {
        if (tank.getBullet() != null && bullet != tank.getBullet()
                && bullet.getBounds().overlaps(tank.getBullet().getBounds())) {

            bullet.respondBulletCollision();
            tank.getBullet().respondBulletCollision();
        }
    }

    private void verifyFortressCollision(Tank tank) {
        if (tank.getBounds().overlaps(fortress.getBounds())) {
            tank.respondTankCollision();
        }
    }

    private void verifyFortressCollision(Bullet bullet) {
        if (bullet.getBounds().overlaps(fortress.getBounds())) {
            world.setState(World.State.GAME_OVER);
        }
    }
}