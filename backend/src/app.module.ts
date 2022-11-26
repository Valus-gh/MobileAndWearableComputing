import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { UsersService } from './users/users.service';
import { JwtModule } from '@nestjs/jwt';
import { JwtAuthStrategy } from './auth.strategy';
import { PassportModule } from '@nestjs/passport';
import { User } from './app.entities';

@Module({
  imports: [
    ConfigModule.forRoot({
      envFilePath: '.env.local'
    }),
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (config: ConfigService) => (
        {
          type: 'mysql',
          host: config.getOrThrow('DB_HOST'),
          port: 3306,
          username: config.getOrThrow('DB_USER'),
          password: config.getOrThrow('DB_PASS'),
          database: config.getOrThrow('DB_NAME'),
          entities: [User],
          synchronize: true,
        }
      )
    }),
    TypeOrmModule.forFeature([User]),
    PassportModule,
    JwtModule.registerAsync({
        useFactory: (configService: ConfigService) => ({
          secret: configService.getOrThrow('JWT_SECRET'),
          signOptions: { expiresIn: '60h' },
        }),
        inject: [ConfigService],
        imports: [ConfigModule]
      })
  ],
  controllers: [AppController],
  providers: [AppService, UsersService, JwtAuthStrategy],
})
export class AppModule {}
