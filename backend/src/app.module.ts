import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { UsersService } from './users/users.service';
import { JwtAuthStrategy } from './users/auth.strategy';
import { DailyStepsModule } from './daily-steps/daily-steps.module';
import { UsersModule } from './users/users.module';
import { DailyStepsService } from './daily-steps/daily-steps.service';
import { faker } from '@faker-js/faker';

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
export class AppModule {
	constructor(
		private readonly usersService: UsersService,
		private readonly dailyStepsService: DailyStepsService,
	) {}

	// TODO test this
	onModuleInit() {
		const users = [
			{ username: 'walker', password: 'walker', goal: 4000 },
			...Array.from({ length: 5 }, () => ({
				username: faker.internet.userName(),
				password: faker.internet.password(),
				goal: faker.datatype.number({ min: 3000, max: 10000 }),
			})),
		];

		const days = Array.from({ length: 60 }, (_, i) => {
			const date = new Date();
			date.setDate(date.getDate() - i);
			return date.toISOString().split('T')[0];
		});

		const steps = Array.from({ length: 10000 }, () =>
			faker.datatype.number({ min: 3000, max: 10000 }),
		);

		users.forEach(async (user) => {
			const { username, password, goal } = user;

			// check if the user already exists before creating it
			const existingUser = await this.usersService.findOneBy({
				username,
			});

			if (existingUser) return;

			const savedUser = await this.usersService.registerUser({
				username,
				password,
			});

			await this.usersService.setUserGoal(savedUser, goal);
			days.forEach(async (day) => {
				await this.dailyStepsService.set(
					day,
					steps[
						faker.datatype.number({ min: 0, max: steps.length - 1 })
					],
					username,
				);
			});
		});
	}
}
