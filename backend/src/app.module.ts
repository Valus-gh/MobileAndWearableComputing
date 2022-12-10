import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { UsersService } from './users/users.service';
import { JwtAuthStrategy } from './users/auth.strategy';
import { DailyStepsModule } from './daily-steps/daily-steps.module';
import { UsersModule } from './users/users.module';

@Module({
	imports: [
		ConfigModule.forRoot({
			envFilePath: '.env.local',
		}),
		TypeOrmModule.forRootAsync({
			imports: [ConfigModule],
			inject: [ConfigService],
			useFactory: (config: ConfigService) => ({
				type: 'mysql',
				host: config.getOrThrow('DB_HOST'),
				port: 3306,
				username: config.getOrThrow('DB_USER'),
				password: config.getOrThrow('DB_PASS'),
				database: config.getOrThrow('DB_NAME'),
				synchronize: true,
				autoLoadEntities: true,
			}),
		}),

		UsersModule,
		DailyStepsModule,
	],
})
export class AppModule {}
