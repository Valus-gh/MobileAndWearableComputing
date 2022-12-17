import { DailySteps } from 'src/daily-steps/daily-steps.entity';
import { Column, Entity, OneToMany, PrimaryColumn } from 'typeorm';

@Entity()
export class User {
	@PrimaryColumn()
	username: string;

	@Column()
	password: string;

	@OneToMany((type) => DailySteps, (dailySteps) => dailySteps.user)
	dailySteps: Promise<DailySteps[]>;

	@Column()
	goal: number;
}
