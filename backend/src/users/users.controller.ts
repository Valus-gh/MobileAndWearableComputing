import {
	Controller,
	Get,
	UseGuards,
	Req,
	Post,
	Body,
	Logger,
} from '@nestjs/common';
import { JwtAuthGuard } from 'src/users/jwt.guard';
import { CredentialsDto, TokensDto } from './users.dto';
import { User } from './users.entities';
import { UsersService } from './users.service';
import { Request } from 'express';

@Controller()
export class UsersController {
	private logger = new Logger(UsersController.name);

	constructor(private readonly usersService: UsersService) {}

	@Get('hello')
	getHello(): string {
		this.logger.log('Hello World!');
		return 'Hello World!';
	}

	@Get('helloUser')
	@UseGuards(JwtAuthGuard)
	getHelloUser(@Req() req: Request): string {
		this.logger.log(`Hello ${(req.user as User).username}`);
		return `Hello ${(req.user as User).username}`;
	}

	@Post('register')
	async register(@Body() registerDto: CredentialsDto): Promise<TokensDto> {
		this.logger.log(`Registering user ${registerDto.username}`);
		await this.usersService.registerUser(registerDto);
		return { accessToken: await this.usersService.loginUser(registerDto) };
	}

	@Post('login')
	async login(@Body() loginDto: CredentialsDto): Promise<TokensDto> {
		this.logger.log(`Logging in user ${loginDto.username}`);
		return {
			accessToken: await this.usersService.loginUser(loginDto),
		};
	}
}
