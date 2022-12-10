import {
	Body,
	Controller,
	Delete,
	Get,
	Logger,
	Param,
	Post,
	Query,
	Req,
	UseGuards,
} from '@nestjs/common';
import { JwtAuthGuard } from 'src/users/jwt.guard';
import { DailyStepsService } from './daily-steps.service';
import { Request } from 'express';
import { User } from 'src/users/users.entities';
import { DailyStepsDto } from './daily-steps.dto';

@Controller('daily-steps')
export class DailyStepsController {
	private logger = new Logger(DailyStepsController.name);
	constructor(private readonly dailyStepsService: DailyStepsService) {}

	@Get('except-user')
	@UseGuards(JwtAuthGuard)
	async getAllExceptUser(
		@Req() req: Request,
	): Promise<{ items: DailyStepsDto[] }> {
		this.logger.log(
			'[' + (req.user as User).username + '] get all except user',
		);

		const resp = await this.dailyStepsService.getAllExceptUser(
			(req.user as User).username,
		);

		return {
			items: resp?.map((r) => {
				return {
					date: r?.date,
					steps: r?.steps,
				};
			}),
		};
	}

	@Get('/:date')
	@UseGuards(JwtAuthGuard)
	async get(
		@Req() req: Request,
		@Param('date') date: string,
	): Promise<DailyStepsDto> {
		this.logger.log(
			'[' + (req.user as User).username + '] get steps at ' + date,
		);
		const resp = await this.dailyStepsService.get(
			date,
			(req.user as User).username,
		);

		return {
			date: resp?.date,
			steps: resp?.steps,
		};
	}

	@Get()
	@UseGuards(JwtAuthGuard)
	async getAll(@Req() req: Request): Promise<{ items: DailyStepsDto[] }> {
		this.logger.log('[' + (req.user as User).username + '] get all steps');
		const resp = await this.dailyStepsService.getAll(
			(req.user as User).username,
		);
		return {
			items: resp?.map((r) => {
				return {
					date: r?.date,
					steps: r?.steps,
				};
			}),
		};
	}

	@Post()
	@UseGuards(JwtAuthGuard)
	async set(
		@Req() req: Request,
		@Body() body: DailyStepsDto,
	): Promise<DailyStepsDto> {
		this.logger.log(
			'[' +
				(req.user as User).username +
				'] set steps at ' +
				body.date +
				' to ' +
				body.steps,
		);
		const resp = await this.dailyStepsService.set(
			body.date,
			body.steps,
			(req.user as User).username,
		);

		return {
			date: resp?.date,
			steps: resp?.steps,
		};
	}

	@Delete()
	@UseGuards(JwtAuthGuard)
	async delete(
		@Req() req: Request,
		@Query('date') date: string,
	): Promise<void> {
		this.logger.log(
			'[' + (req.user as User).username + '] delete steps at ' + date,
		);
		await this.dailyStepsService.delete(date, (req.user as User).username);
		return;
	}
}
