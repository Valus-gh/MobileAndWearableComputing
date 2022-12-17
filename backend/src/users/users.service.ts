import { Injectable, UnauthorizedException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { JwtService } from '@nestjs/jwt';
import { Repository } from 'typeorm';
import { CredentialsDto } from './users.dto';
import { User } from './users.entities';

@Injectable()
export class UsersService {
	constructor(
		@InjectRepository(User) private userRepo: Repository<User>,
		private jwtService: JwtService,
	) {}

	async findOneBy(criteria: object) {
		return await this.userRepo.findOneBy(criteria);
	}

	async registerUser(credentials: CredentialsDto) {
		const u = this.userRepo.create();
		u.password = credentials.password;
		u.username = credentials.username;
		u.goal = 4000;
		return await this.userRepo.save(u);
	}

	async loginUser({ username, password }: CredentialsDto) {
		const u = await this.userRepo.findOneBy({ username });
		if (!u || u.password !== password) throw new UnauthorizedException();

		return this.jwtService.sign({ sub: username });
	}

	async validateJWT({ sub }: object & { sub: string }): Promise<User> {
		const user = await this.userRepo.findOneBy({ username: sub });
		if (!user) return null;

		return user;
	}

	async setUserGoal(user: User, goal: number) {
		user.goal = goal;
		return await this.userRepo.save(user);
	}

	async getUserGoal(user: User) {
		return user.goal;
	}
}
