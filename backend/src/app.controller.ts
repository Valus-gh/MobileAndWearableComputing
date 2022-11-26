import {Body, Controller, Get, Post, Req, UseGuards} from '@nestjs/common';
import {AppService} from './app.service';
import {CredentialsDto, TokensDto} from "./app.dto";
import { UsersService } from './users/users.service';
import { JwtAuthGuard } from './jwt.guard';
import {Request} from 'express';
import { User } from './app.entities';

@Controller()
export class AppController {
    constructor(
        private readonly appService: AppService,
        private readonly usersService: UsersService
        ) {
    }

    @Get('hello')
    getHello(): string {
        return this.appService.getHello();
    }

    @Get('helloUser')
    @UseGuards(JwtAuthGuard)
    getHelloUser(@Req() req: Request): string {
        return `Hello ${(req.user as User).username}`
    }

    @Post('register')
    async register(@Body() registerDto: CredentialsDto): Promise<TokensDto> {
        await this.usersService.registerUser(registerDto)
        return { accessToken: await this.usersService.loginUser(registerDto)}
    }

    @Post('login')
    async login(@Body() loginDto: CredentialsDto): Promise<TokensDto> {
        return {
            accessToken: await this.usersService.loginUser(loginDto)
        }
    }
}
