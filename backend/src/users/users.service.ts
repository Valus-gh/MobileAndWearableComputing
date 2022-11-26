import { Injectable, UnauthorizedException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { JwtService } from '@nestjs/jwt';
import { CredentialsDto } from 'src/app.dto';
import { User } from 'src/app.entities';
import { Repository } from 'typeorm';

@Injectable()
export class UsersService {
    constructor(
        @InjectRepository(User) private userRepo: Repository<User>,
        private jwtService: JwtService
        ){}

    async registerUser(credentials: CredentialsDto) {
        const u = this.userRepo.create()
        u.password = credentials.password
        u.username = credentials.username
        return await this.userRepo.save(u)
    }

    async loginUser({username, password}: CredentialsDto) {
        const u = await this.userRepo.findOneBy({username})
        if(!u || u.password !== password)
            throw new UnauthorizedException()
        
        return this.jwtService.sign({sub: username})
    }

    async validateJWT({sub}: object & {sub: string}): Promise<User> {
        const user = await this.userRepo.findOneBy({username: sub})
        if(!user)
            return null

        return user
    }
}
