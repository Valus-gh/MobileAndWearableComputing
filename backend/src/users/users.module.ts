import { Module } from '@nestjs/common';
import { ConfigService, ConfigModule } from '@nestjs/config';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from './users.entities';
import { UsersController } from './users.controller';
import { UsersService } from './users.service';
import { JwtAuthStrategy } from './auth.strategy';

@Module({
	controllers: [UsersController],
	imports: [
		TypeOrmModule.forFeature([User]),
		PassportModule,
		JwtModule.registerAsync({
			useFactory: (configService: ConfigService) => ({
				secret: configService.getOrThrow('JWT_SECRET'),
				signOptions: { expiresIn: '60h' },
			}),
			inject: [ConfigService],
			imports: [ConfigModule],
		}),
		ConfigModule,
	],
	providers: [UsersService, JwtAuthStrategy],
	exports: [UsersService],
})
export class UsersModule {}
