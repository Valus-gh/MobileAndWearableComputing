import { User } from 'src/users/users.entities';
import {
	Column,
	Entity,
	Generated,
	Index,
	ManyToOne,
	PrimaryColumn,
} from 'typeorm';

@Entity()
export class DailySteps {
	@PrimaryColumn()
	@Generated('uuid')
	id: number;

	@Column()
	steps: number;

	@Index()
	@Column()
	date: string;

	@ManyToOne((type) => User, (user) => user.dailySteps)
	user: Promise<User>;

	constructor(steps: number, date: string) {}
}
