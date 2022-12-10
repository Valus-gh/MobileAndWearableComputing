import { Get, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from 'src/users/users.entities';
import { UsersService } from 'src/users/users.service';
import { LessThan, MoreThan, Not, Repository } from 'typeorm';
import { DailySteps } from './daily-steps.entity';

@Injectable()
export class DailyStepsService {
	constructor(
		@InjectRepository(DailySteps)
		private dailyStepsRepo: Repository<DailySteps>,
		private userService: UsersService,
	) {}

	async get(date: string, username: string) {
		return await this.dailyStepsRepo.findOneBy({
			date,
			user: { username },
		});
	}

	async set(date: string, steps: number, username: string) {
		const dailySteps = await this.get(date, username);
		if (dailySteps) {
			dailySteps.steps = steps;
			return await this.dailyStepsRepo.save(dailySteps);
		} else {
			const newDailySteps = this.dailyStepsRepo.create();
			newDailySteps.date = date;
			newDailySteps.steps = steps;
			newDailySteps.user = Promise.resolve(
				await this.userService.findOneBy({ username }),
			);
			return await this.dailyStepsRepo.save(newDailySteps);
		}
	}

	async getAll(username: string) {
		return await this.dailyStepsRepo.find({
			where: {
				user: { username },
			},
		});
	}

	async getAllExceptUser(username: string) {
		return await this.dailyStepsRepo.find({
			where: {
				user: { username: Not(username) },
			},
		});
	}

	async delete(date: string, username: string) {
		return await this.dailyStepsRepo.delete({
			date,
			user: { username },
		});
	}
}
