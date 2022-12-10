import { Controller, Get, UseGuards, Req, Post, Body } from '@nestjs/common';
import { JwtAuthGuard } from 'src/users/jwt.guard';
import { CredentialsDto, TokensDto } from './users.dto';
import { User } from './users.entities';
import { UsersService } from './users.service';
import { Request } from 'express';

@Controller()
export class UsersController {
	constructor(private readonly usersService: UsersService) {}

	@Get('hello')
	getHello(): string {
		return 'Hello World!';
	}

	@Get('helloUser')
	@UseGuards(JwtAuthGuard)
	getHelloUser(@Req() req: Request): string {
		return `Hello ${(req.user as User).username}`;
	}

	@Post('register')
	async register(@Body() registerDto: CredentialsDto): Promise<TokensDto> {
		await this.usersService.registerUser(registerDto);
		return { accessToken: await this.usersService.loginUser(registerDto) };
	}

	@Post('login')
	async login(@Body() loginDto: CredentialsDto): Promise<TokensDto> {
		return {
			accessToken: await this.usersService.loginUser(loginDto),
		};
	}
}
